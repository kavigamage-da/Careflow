package com.example.data.repository

import com.example.core.model.AuditAction
import com.example.core.model.DepartmentQueueMetrics
import com.example.core.model.QueuePriority
import com.example.core.model.QueueStatus
import com.example.core.model.QueueTicket
import com.example.core.security.SessionManager
import com.example.core.util.Resource
import com.example.data.local.CareFlowDatabase
import com.example.data.local.entity.QueueTicketEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.UUID

class QueueRepositoryImpl(
    private val database: CareFlowDatabase,
    private val sessionManager: SessionManager,
    private val auditRepository: AuditRepository
) : QueueRepository {

    private val queueDao = database.queueDao()
    private val patientDao = database.patientDao()
    private val departmentDao = database.departmentDao()
    private val doctorDao = database.doctorDao()

    override fun getActiveQueue(departmentId: String?): Flow<List<QueueTicket>> {
        val flow = if (departmentId.isNullOrBlank() || departmentId == "ALL") {
            queueDao.getActiveQueue()
        } else {
            queueDao.getQueueByDepartment(departmentId)
        }
        return flow.map { entities -> entities.map { it.toDomain() } }
    }

    override fun getWaitingTickets(departmentId: String?): Flow<List<QueueTicket>> {
        val flow = if (departmentId.isNullOrBlank() || departmentId == "ALL") {
            queueDao.getActiveQueue().map { list -> list.filter { it.status == "WAITING" } }
        } else {
            queueDao.getWaitingTicketsByDepartment(departmentId)
        }
        return flow.map { entities -> entities.map { it.toDomain() } }
    }

    override fun getServingTickets(departmentId: String?): Flow<List<QueueTicket>> {
        val dept = if (departmentId.isNullOrBlank() || departmentId == "ALL") null else departmentId
        return queueDao.getServingTickets(dept).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTicketById(id: String): Flow<QueueTicket?> {
        return queueDao.getTicketByIdFlow(id).map { it?.toDomain() }
    }

    override fun getPatientActiveTicket(patientId: String): Flow<QueueTicket?> {
        return queueDao.getActiveTicketForPatientFlow(patientId).map { it?.toDomain() }
    }

    override suspend fun createTicket(
        patientId: String,
        departmentId: String,
        doctorId: String?,
        priority: QueuePriority,
        appointmentId: String?,
        notes: String?
    ): Resource<QueueTicket> {
        return try {
            val patient = patientDao.getPatientById(patientId)
                ?: return Resource.Error("Patient not found: $patientId")

            // Prevent duplicate active ticket
            val existing = queueDao.getActiveTicketForPatient(patientId)
            if (existing != null) {
                return Resource.Error("Patient already has an active ticket (${existing.ticketNumber}) in ${existing.status}")
            }

            // Generate ticket number: e.g. A-101, C-102 based on dept
            val prefix = when (departmentId) {
                "dept_cardio" -> "C"
                "dept_gen_med" -> "G"
                "dept_pediatrics" -> "P"
                "dept_ortho" -> "O"
                "dept_emergency" -> "E"
                else -> "A"
            }
            val startOfDay = getStartOfDayMillis()
            val dailyCount = queueDao.getDailyTicketCountForDepartment(departmentId, startOfDay)
            val ticketNumber = "$prefix-${101 + dailyCount}"

            val currentUser = sessionManager.getCurrentUser()
            val userId = currentUser?.id ?: "system"
            val now = System.currentTimeMillis()

            val estimatedWait = when (priority) {
                QueuePriority.EMERGENCY -> 0
                QueuePriority.URGENT -> 5
                QueuePriority.FAST_TRACK -> 10
                QueuePriority.STANDARD -> 20
            }

            val entity = QueueTicketEntity(
                id = UUID.randomUUID().toString(),
                ticketNumber = ticketNumber,
                patientId = patientId,
                departmentId = departmentId,
                doctorId = doctorId,
                appointmentId = appointmentId,
                priority = priority.level,
                priorityAssignedBy = if (priority != QueuePriority.STANDARD) userId else null,
                status = "WAITING",
                checkInTime = now,
                estimatedWaitMinutes = estimatedWait,
                notes = notes,
                createdAt = now,
                updatedAt = now,
                createdBy = userId,
                updatedBy = userId
            )

            queueDao.insertTicket(entity)

            auditRepository.recordAction(
                action = AuditAction.QUEUE_TICKET_ISSUED,
                entityName = "QueueTicket",
                entityId = entity.id,
                details = "Issued ticket $ticketNumber (${priority.displayName}) for patient ${patient.fullName} (${patient.hospitalRegNo})"
            )

            Resource.Success(entity.toDomain())
        } catch (e: Exception) {
            Resource.Error("Failed to issue queue ticket: ${e.message}")
        }
    }

    override suspend fun callNextPatient(departmentId: String, doctorId: String?): Resource<QueueTicket> {
        return try {
            val nextTicket = queueDao.getNextWaitingTicket(departmentId, doctorId)
                ?: return Resource.Error("No waiting patients in queue for department $departmentId")

            val currentUser = sessionManager.getCurrentUser()
            val userId = currentUser?.id ?: "staff"
            val now = System.currentTimeMillis()

            // Atomic concurrency guard: ensures ticket is still WAITING at moment of update
            val rowsUpdated = queueDao.callTicketIfWaiting(
                ticketId = nextTicket.id,
                calledBy = userId,
                calledAt = now,
                updatedAt = now,
                updatedBy = userId
            )

            if (rowsUpdated == 0) {
                return Resource.Error("Ticket ${nextTicket.ticketNumber} was already called by another operator. Please refresh.")
            }

            val updated = nextTicket.copy(
                status = "CALLED",
                calledAt = now,
                calledBy = userId,
                updatedAt = now,
                updatedBy = userId
            )

            auditRepository.recordAction(
                action = AuditAction.QUEUE_PATIENT_CALLED,
                entityName = "QueueTicket",
                entityId = updated.id,
                details = "Called ticket ${updated.ticketNumber} for consultation by $userId"
            )

            Resource.Success(updated.toDomain())
        } catch (e: Exception) {
            Resource.Error("Failed to call next patient: ${e.message}")
        }
    }

    override suspend fun recallPatient(ticketId: String): Resource<QueueTicket> {
        return try {
            val ticket = queueDao.getTicketById(ticketId)
                ?: return Resource.Error("Ticket not found: $ticketId")

            val currentUser = sessionManager.getCurrentUser()
            val userId = currentUser?.id ?: "staff"
            val now = System.currentTimeMillis()

            val updated = ticket.copy(
                status = "CALLED",
                calledAt = now,
                calledBy = userId,
                recallCount = ticket.recallCount + 1,
                updatedAt = now,
                updatedBy = userId
            )

            queueDao.updateTicket(updated)

            auditRepository.recordAction(
                action = AuditAction.QUEUE_PATIENT_RECALLED,
                entityName = "QueueTicket",
                entityId = updated.id,
                details = "Recalled ticket ${updated.ticketNumber} (Attempt ${updated.recallCount})"
            )

            Resource.Success(updated.toDomain())
        } catch (e: Exception) {
            Resource.Error("Failed to recall patient: ${e.message}")
        }
    }

    override suspend fun startConsultation(ticketId: String, doctorId: String?): Resource<QueueTicket> {
        return try {
            val ticket = queueDao.getTicketById(ticketId)
                ?: return Resource.Error("Ticket not found: $ticketId")

            val currentUser = sessionManager.getCurrentUser()
            val userId = doctorId ?: currentUser?.id ?: "doctor"
            val now = System.currentTimeMillis()

            val updated = ticket.copy(
                status = "IN_CONSULTATION",
                consultationStartTime = now,
                consultationBy = userId,
                doctorId = doctorId ?: ticket.doctorId,
                updatedAt = now,
                updatedBy = userId
            )

            queueDao.updateTicket(updated)

            auditRepository.recordAction(
                action = AuditAction.CONSULTATION_STARTED,
                entityName = "QueueTicket",
                entityId = updated.id,
                details = "Consultation commenced for ticket ${updated.ticketNumber} by $userId"
            )

            Resource.Success(updated.toDomain())
        } catch (e: Exception) {
            Resource.Error("Failed to start consultation: ${e.message}")
        }
    }

    override suspend fun completeConsultation(ticketId: String): Resource<QueueTicket> {
        return try {
            val ticket = queueDao.getTicketById(ticketId)
                ?: return Resource.Error("Ticket not found: $ticketId")

            val currentUser = sessionManager.getCurrentUser()
            val userId = currentUser?.id ?: "staff"
            val now = System.currentTimeMillis()

            val updated = ticket.copy(
                status = "COMPLETED",
                completedAt = now,
                completedBy = userId,
                updatedAt = now,
                updatedBy = userId
            )

            queueDao.updateTicket(updated)

            auditRepository.recordAction(
                action = AuditAction.CONSULTATION_COMPLETED,
                entityName = "QueueTicket",
                entityId = updated.id,
                details = "Completed queue cycle for ticket ${updated.ticketNumber}"
            )

            Resource.Success(updated.toDomain())
        } catch (e: Exception) {
            Resource.Error("Failed to complete ticket: ${e.message}")
        }
    }

    override suspend fun skipPatient(ticketId: String, reason: String): Resource<QueueTicket> {
        return try {
            val ticket = queueDao.getTicketById(ticketId)
                ?: return Resource.Error("Ticket not found: $ticketId")

            val currentUser = sessionManager.getCurrentUser()
            val userId = currentUser?.id ?: "staff"
            val now = System.currentTimeMillis()

            val updated = ticket.copy(
                status = "SKIPPED",
                skippedTime = now,
                notes = if (ticket.notes.isNullOrBlank()) reason else "${ticket.notes} | Skipped: $reason",
                updatedAt = now,
                updatedBy = userId
            )

            queueDao.updateTicket(updated)

            auditRepository.recordAction(
                action = AuditAction.QUEUE_PATIENT_SKIPPED,
                entityName = "QueueTicket",
                entityId = updated.id,
                details = "Skipped ticket ${updated.ticketNumber} ($reason)"
            )

            Resource.Success(updated.toDomain())
        } catch (e: Exception) {
            Resource.Error("Failed to skip patient: ${e.message}")
        }
    }

    override suspend fun cancelTicket(ticketId: String, reason: String): Resource<QueueTicket> {
        return try {
            val ticket = queueDao.getTicketById(ticketId)
                ?: return Resource.Error("Ticket not found: $ticketId")

            val currentUser = sessionManager.getCurrentUser()
            val userId = currentUser?.id ?: "staff"
            val now = System.currentTimeMillis()

            val updated = ticket.copy(
                status = "CANCELLED",
                cancelledTime = now,
                notes = if (ticket.notes.isNullOrBlank()) "Cancelled: $reason" else "${ticket.notes} | Cancelled: $reason",
                updatedAt = now,
                updatedBy = userId
            )

            queueDao.updateTicket(updated)

            auditRepository.recordAction(
                action = AuditAction.QUEUE_PATIENT_CANCELLED,
                entityName = "QueueTicket",
                entityId = updated.id,
                details = "Cancelled ticket ${updated.ticketNumber}: $reason"
            )

            Resource.Success(updated.toDomain())
        } catch (e: Exception) {
            Resource.Error("Failed to cancel ticket: ${e.message}")
        }
    }

    override suspend fun transferDepartment(ticketId: String, targetDepartmentId: String, reason: String): Resource<QueueTicket> {
        return try {
            val ticket = queueDao.getTicketById(ticketId)
                ?: return Resource.Error("Ticket not found: $ticketId")

            val currentUser = sessionManager.getCurrentUser()
            val userId = currentUser?.id ?: "staff"
            val now = System.currentTimeMillis()

            val updated = ticket.copy(
                transferSourceDepartmentId = ticket.departmentId,
                departmentId = targetDepartmentId,
                transferReason = reason,
                status = "WAITING",
                updatedAt = now,
                updatedBy = userId
            )

            queueDao.updateTicket(updated)

            auditRepository.recordAction(
                action = AuditAction.QUEUE_PATIENT_TRANSFERRED,
                entityName = "QueueTicket",
                entityId = updated.id,
                details = "Transferred ticket ${updated.ticketNumber} from ${ticket.departmentId} to $targetDepartmentId: $reason"
            )

            Resource.Success(updated.toDomain())
        } catch (e: Exception) {
            Resource.Error("Failed to transfer ticket: ${e.message}")
        }
    }

    override suspend fun updatePriority(ticketId: String, newPriority: QueuePriority, reason: String): Resource<QueueTicket> {
        return try {
            val ticket = queueDao.getTicketById(ticketId)
                ?: return Resource.Error("Ticket not found: $ticketId")

            val currentUser = sessionManager.getCurrentUser()
            val userId = currentUser?.id ?: "staff"
            val now = System.currentTimeMillis()

            val updated = ticket.copy(
                priority = newPriority.level,
                priorityAssignedBy = userId,
                notes = if (ticket.notes.isNullOrBlank()) "Priority set to ${newPriority.displayName}: $reason" else "${ticket.notes} | Priority: ${newPriority.displayName}",
                updatedAt = now,
                updatedBy = userId
            )

            queueDao.updateTicket(updated)

            auditRepository.recordAction(
                action = AuditAction.QUEUE_PRIORITY_CHANGED,
                entityName = "QueueTicket",
                entityId = updated.id,
                details = "Changed ticket ${updated.ticketNumber} priority to ${newPriority.displayName}: $reason"
            )

            Resource.Success(updated.toDomain())
        } catch (e: Exception) {
            Resource.Error("Failed to update priority: ${e.message}")
        }
    }

    override fun getDepartmentMetrics(departmentId: String): Flow<DepartmentQueueMetrics> {
        val startOfDay = getStartOfDayMillis()
        val waitingFlow = queueDao.getWaitingCountByDepartment(departmentId)
        val calledFlow = queueDao.getCalledCountByDepartment(departmentId)
        val inConsultFlow = queueDao.getInConsultationCountByDepartment(departmentId)
        val completedFlow = queueDao.getCompletedTodayCountByDepartment(departmentId, startOfDay)

        return combine(waitingFlow, calledFlow, inConsultFlow, completedFlow) { waiting, called, inConsult, completed ->
            DepartmentQueueMetrics(
                departmentId = departmentId,
                departmentName = departmentId.replace("dept_", "").replace("_", " ").capitalizeWords(),
                waitingCount = waiting,
                calledCount = called,
                inConsultationCount = inConsult,
                completedTodayCount = completed,
                averageWaitMinutes = if (waiting > 0) waiting * 12 else 5
            )
        }
    }

    private suspend fun QueueTicketEntity.toDomain(): QueueTicket {
        val patient = patientDao.getPatientById(patientId)
        val departmentName = when (departmentId) {
            "dept_cardio" -> "Cardiology"
            "dept_gen_med" -> "General Medicine"
            "dept_pediatrics" -> "Pediatrics"
            "dept_ortho" -> "Orthopedics"
            "dept_emergency" -> "Emergency"
            else -> departmentId.replace("dept_", "").replace("_", " ").capitalizeWords()
        }
        val doctorName = if (doctorId != null) {
            when (doctorId) {
                "doc_carter" -> "Dr. Sarah Carter"
                "doc_smith" -> "Dr. James Smith"
                "doc_evans" -> "Dr. Robert Evans"
                else -> doctorId
            }
        } else null

        val parsedStatus = try {
            QueueStatus.valueOf(status)
        } catch (e: Exception) {
            QueueStatus.WAITING
        }

        return QueueTicket(
            id = id,
            ticketNumber = ticketNumber,
            patientId = patientId,
            patientName = patient?.fullName ?: "Unknown Patient",
            patientRegNo = patient?.hospitalRegNo ?: "",
            appointmentId = appointmentId,
            departmentId = departmentId,
            departmentName = departmentName,
            doctorId = doctorId,
            doctorName = doctorName,
            priority = QueuePriority.fromLevel(priority),
            priorityAssignedBy = priorityAssignedBy,
            status = parsedStatus,
            checkInTime = checkInTime,
            calledTime = calledAt,
            consultationStartTime = consultationStartTime,
            completedTime = completedAt,
            skippedTime = skippedTime,
            cancelledTime = cancelledTime,
            estimatedWaitMinutes = estimatedWaitMinutes,
            recallCount = recallCount,
            calledBy = calledBy,
            consultationBy = consultationBy,
            completedBy = completedBy,
            transferSourceDepartmentId = transferSourceDepartmentId,
            transferReason = transferReason,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt,
            createdBy = createdBy,
            updatedBy = updatedBy
        )
    }

    private fun getStartOfDayMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}
