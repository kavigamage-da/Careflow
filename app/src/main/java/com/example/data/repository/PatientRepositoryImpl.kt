package com.example.data.repository

import com.example.core.model.AuditAction
import com.example.core.model.Patient
import com.example.core.model.PatientStatus
import com.example.core.model.PatientTimelineEvent
import com.example.core.model.Permission
import com.example.core.model.TimelineEventType
import com.example.core.security.SessionManager
import com.example.core.util.Resource
import com.example.data.local.CareFlowDatabase
import com.example.data.local.entity.AppointmentEntity
import com.example.data.local.entity.ClinicalEncounterEntity
import com.example.data.local.entity.EmergencyContactEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.LabOrderEntity
import com.example.data.local.entity.PatientEntity
import com.example.data.local.entity.PrescriptionEntity
import com.example.data.local.entity.QueueTicketEntity
import com.example.data.local.entity.VitalSignEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

class PatientRepositoryImpl(
    private val database: CareFlowDatabase,
    private val sessionManager: SessionManager,
    private val auditRepository: AuditRepository
) : PatientRepository {

    private val repoScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val patientDao = database.patientDao()
    private val emergencyContactDao = database.emergencyContactDao()
    private val appointmentDao = database.appointmentDao()
    private val clinicalDao = database.clinicalDao()
    private val prescriptionDao = database.prescriptionDao()
    private val labDao = database.labDao()
    private val billingDao = database.billingDao()
    private val queueDao = database.queueDao()

    private data class ClinicalBundle(
        val vitals: List<VitalSignEntity>,
        val encounters: List<ClinicalEncounterEntity>,
        val prescriptions: List<PrescriptionEntity>,
        val labOrders: List<LabOrderEntity>
    )

    override fun getAllPatients(statusFilter: String, sortBy: String): Flow<List<Patient>> {
        return patientDao.getPatientsPaged(status = statusFilter, sortBy = sortBy).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun searchPatients(query: String, statusFilter: String): Flow<List<Patient>> {
        val currentUser = sessionManager.currentUser.value
        val trimmed = query.trim()

        if (currentUser != null && trimmed.isNotBlank()) {
            repoScope.launch {
                auditRepository.recordAction(
                    action = AuditAction.PATIENT_SEARCHED,
                    entityName = "PatientDirectory",
                    entityId = null,
                    details = "Search query: '$trimmed', status: '$statusFilter'"
                )
            }
        }

        return patientDao.searchPatients(query = trimmed, status = statusFilter).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getPatientById(id: String): Flow<Patient?> {
        return patientDao.observePatientById(id).map { entity ->
            entity?.let {
                val currentUser = sessionManager.currentUser.value
                if (currentUser != null) {
                    repoScope.launch {
                        auditRepository.recordAction(
                            action = AuditAction.PATIENT_VIEWED,
                            entityName = "Patient",
                            entityId = it.id,
                            details = "Accessed record for ${it.fullName} (${it.hospitalRegNo})"
                        )
                    }
                }
                it.toDomain()
            }
        }
    }

    override fun getPatientTimeline(patientId: String): Flow<List<PatientTimelineEvent>> {
        val currentUser = sessionManager.currentUser.value
        val canViewClinical = currentUser?.hasPermission(Permission.VIEW_PATIENT_CLINICAL_TIMELINE) == true

        val clinicalFlow: Flow<ClinicalBundle> = combine(
            clinicalDao.getVitalsForPatient(patientId),
            clinicalDao.getEncountersForPatient(patientId),
            prescriptionDao.getPrescriptionsForPatient(patientId),
            labDao.getLabOrdersForPatient(patientId)
        ) { vitals, encounters, rx, labs ->
            ClinicalBundle(vitals, encounters, rx, labs)
        }

        return combine(
            patientDao.observePatientById(patientId),
            appointmentDao.getAppointmentsForPatient(patientId),
            billingDao.getInvoicesForPatient(patientId),
            clinicalFlow
        ) { patient, appts, invoices, clinical ->
            val events = mutableListOf<PatientTimelineEvent>()

            patient?.let { p ->
                events.add(
                    PatientTimelineEvent(
                        id = "reg_${p.id}",
                        timestamp = p.registeredDate,
                        type = TimelineEventType.REGISTRATION,
                        title = "Hospital Registration",
                        department = "Reception & Admitting",
                        practitionerName = if (p.createdBy.isNotBlank()) p.createdBy else "Staff Admitting",
                        summary = "Registered with Hospital ID: ${p.hospitalRegNo}. Primary language: ${p.preferredLanguage}.",
                        status = p.status,
                        isConfidentialClinical = false
                    )
                )
            }

            appts.forEach { apt ->
                events.add(
                    PatientTimelineEvent(
                        id = "apt_${apt.id}",
                        timestamp = apt.dateTime,
                        type = TimelineEventType.APPOINTMENT,
                        title = "Appointment (${apt.appointmentType.replace("_", " ")})",
                        department = apt.departmentId?.replace("dept_", "")?.uppercase() ?: "Outpatient",
                        practitionerName = apt.doctorId ?: "Attending Physician",
                        summary = apt.reason + if (apt.notes.isNotBlank()) " • ${apt.notes}" else "",
                        status = apt.status,
                        isConfidentialClinical = false
                    )
                )
            }

            if (canViewClinical) {
                clinical.vitals.forEach { vit ->
                    val bp = if (vit.systolicBp != null && vit.diastolicBp != null) "${vit.systolicBp}/${vit.diastolicBp} mmHg" else "N/A"
                    val hr = if (vit.pulseRateBpm != null) "${vit.pulseRateBpm} bpm" else "N/A"
                    val temp = if (vit.temperatureCelsius != null) "${vit.temperatureCelsius}°C" else "N/A"
                    val spo2 = if (vit.oxygenSaturationPercent != null) "${vit.oxygenSaturationPercent}%" else "N/A"

                    events.add(
                        PatientTimelineEvent(
                            id = "vit_${vit.id}",
                            timestamp = vit.timestamp,
                            type = TimelineEventType.TRIAGE_VITALS,
                            title = "Nursing Vitals & Triage",
                            department = "Triage Nursing",
                            practitionerName = vit.recordedByUserId,
                            summary = "BP: $bp • HR: $hr • Temp: $temp • SpO2: $spo2" +
                                    if (vit.urgentAttentionFlag) " [URGENT ATTENTION FLAG]" else "",
                            status = if (vit.urgentAttentionFlag) "URGENT" else "NORMAL",
                            isConfidentialClinical = true
                        )
                    )
                }

                clinical.encounters.forEach { enc ->
                    events.add(
                        PatientTimelineEvent(
                            id = "enc_${enc.id}",
                            timestamp = enc.timestamp,
                            type = TimelineEventType.CLINICAL_CONSULTATION,
                            title = "Doctor Clinical Consultation",
                            department = "Consultation Suite",
                            practitionerName = enc.doctorId ?: "Physician",
                            summary = "Chief Complaint: ${enc.chiefComplaint}\nAssessment: ${enc.assessment}\nClinician Diagnosis: ${enc.clinicianDiagnosis}",
                            status = enc.status,
                            isConfidentialClinical = true
                        )
                    )
                }

                clinical.prescriptions.forEach { rx ->
                    events.add(
                        PatientTimelineEvent(
                            id = "rx_${rx.id}",
                            timestamp = rx.createdAt,
                            type = TimelineEventType.PRESCRIPTION,
                            title = "Prescription Issued",
                            department = "Pharmacy Services",
                            practitionerName = rx.doctorId ?: "Prescribing Physician",
                            summary = "Instructions: ${rx.instructions}",
                            status = rx.status,
                            isConfidentialClinical = true
                        )
                    )
                }

                clinical.labOrders.forEach { lab ->
                    events.add(
                        PatientTimelineEvent(
                            id = "lab_${lab.id}",
                            timestamp = lab.orderedAt,
                            type = TimelineEventType.LAB_ORDER,
                            title = "Laboratory Investigation (${lab.testName})",
                            department = "Pathology & Diagnostics",
                            practitionerName = lab.doctorId ?: "Ordering Clinician",
                            summary = "Priority: ${lab.priority} • Sample: ${lab.sampleType}" +
                                    if (lab.resultJson != null) " • Result Ready" else "",
                            status = lab.status,
                            isConfidentialClinical = true
                        )
                    )
                }
            }

            invoices.forEach { inv ->
                events.add(
                    PatientTimelineEvent(
                        id = "inv_${inv.id}",
                        timestamp = inv.createdAt,
                        type = TimelineEventType.INVOICE,
                        title = "Invoice ${inv.invoiceNumber}",
                        department = "Patient Accounts",
                        practitionerName = "Billing Cashier",
                        summary = "Total: $${inv.totalAmount} • Settled: $${inv.paidAmount}",
                        status = inv.status,
                        isConfidentialClinical = false
                    )
                )
            }

            events.sortedByDescending { it.timestamp }
        }
    }

    override suspend fun checkPossibleDuplicates(
        phone: String,
        email: String,
        nationalId: String,
        fullName: String,
        dob: String
    ): List<Patient> {
        val matches = patientDao.findPossibleDuplicates(
            phone = phone.trim(),
            email = email.trim(),
            nationalId = nationalId.trim(),
            fullName = fullName.trim(),
            dob = dob.trim()
        )

        auditRepository.recordAction(
            action = AuditAction.DUPLICATE_CHECK_PERFORMED,
            entityName = "PatientRegistration",
            entityId = null,
            details = "Duplicate check ran for phone: '$phone', email: '$email', matches found: ${matches.size}"
        )

        return matches.map { it.toDomain() }
    }

    override suspend fun registerPatient(patient: Patient): Resource<Patient> {
        val currentUser = sessionManager.currentUser.value
        if (currentUser == null || !currentUser.hasPermission(Permission.REGISTER_PATIENT)) {
            auditRepository.recordAction(
                action = AuditAction.UNAUTHORIZED_ACCESS_ATTEMPT,
                entityName = "Patient",
                entityId = patient.id,
                details = "Attempted patient registration without REGISTER_PATIENT permission",
                result = "BLOCKED"
            )
            return Resource.Error("Unauthorized: You do not have permission to register patients")
        }

        val now = System.currentTimeMillis()
        val regNo = if (patient.hospitalRegNo.isBlank()) generateHospitalRegNo() else patient.hospitalRegNo

        val entity = PatientEntity(
            id = patient.id.ifBlank { UUID.randomUUID().toString() },
            hospitalRegNo = regNo,
            fullName = patient.fullName.trim(),
            dob = patient.dob.trim(),
            gender = patient.gender,
            nationalIdOrPassport = patient.nationalIdOrPassport.trim(),
            phone = patient.phone.trim(),
            email = patient.email.trim(),
            address = patient.address.trim(),
            preferredLanguage = patient.preferredLanguage.ifBlank { "English" },
            bloodGroup = patient.bloodGroup,
            allergies = patient.allergies.trim(),
            existingConditions = patient.existingConditions.trim(),
            emergencyContactName = patient.emergencyContactName.trim(),
            emergencyContactRelationship = patient.emergencyContactRelationship.ifBlank { "Family" },
            emergencyContactPhone = patient.emergencyContactPhone.trim(),
            emergencyContactSecondaryPhone = patient.emergencyContactSecondaryPhone.trim(),
            registeredDate = now,
            status = PatientStatus.ACTIVE.name,
            createdAt = now,
            updatedAt = now,
            createdBy = currentUser.username,
            updatedBy = currentUser.username
        )

        patientDao.insertPatient(entity)

        // Also record primary emergency contact in dedicated table
        val emergencyContact = EmergencyContactEntity(
            id = UUID.randomUUID().toString(),
            patientId = entity.id,
            name = entity.emergencyContactName,
            relationship = entity.emergencyContactRelationship,
            phone = entity.emergencyContactPhone,
            secondaryPhone = entity.emergencyContactSecondaryPhone,
            address = entity.address,
            isPrimary = true,
            createdAt = now,
            updatedAt = now
        )
        emergencyContactDao.insertContact(emergencyContact)

        auditRepository.recordAction(
            action = AuditAction.PATIENT_CREATED,
            entityName = "Patient",
            entityId = entity.id,
            details = "New patient registered: ${entity.fullName}, Reg No: ${entity.hospitalRegNo}"
        )

        return Resource.Success(entity.toDomain())
    }

    override suspend fun updatePatientDemographics(patient: Patient): Resource<Unit> {
        val currentUser = sessionManager.currentUser.value
        if (currentUser == null || !currentUser.hasPermission(Permission.EDIT_PATIENT)) {
            auditRepository.recordAction(
                action = AuditAction.UNAUTHORIZED_ACCESS_ATTEMPT,
                entityName = "Patient",
                entityId = patient.id,
                details = "Attempted demographic update without EDIT_PATIENT permission",
                result = "BLOCKED"
            )
            return Resource.Error("Unauthorized: You do not have permission to update patient demographics")
        }

        val existing = patientDao.getPatientById(patient.id)
            ?: return Resource.Error("Patient not found")

        val now = System.currentTimeMillis()
        val updated = existing.copy(
            fullName = patient.fullName.trim(),
            dob = patient.dob.trim(),
            gender = patient.gender,
            nationalIdOrPassport = patient.nationalIdOrPassport.trim(),
            phone = patient.phone.trim(),
            email = patient.email.trim(),
            address = patient.address.trim(),
            preferredLanguage = patient.preferredLanguage,
            bloodGroup = patient.bloodGroup,
            allergies = patient.allergies.trim(),
            existingConditions = patient.existingConditions.trim(),
            emergencyContactName = patient.emergencyContactName.trim(),
            emergencyContactRelationship = patient.emergencyContactRelationship,
            emergencyContactPhone = patient.emergencyContactPhone.trim(),
            emergencyContactSecondaryPhone = patient.emergencyContactSecondaryPhone.trim(),
            updatedAt = now,
            updatedBy = currentUser.username
        )

        patientDao.updatePatient(updated)

        auditRepository.recordAction(
            action = AuditAction.PATIENT_UPDATED,
            entityName = "Patient",
            entityId = patient.id,
            details = "Updated demographics for patient ${patient.fullName} (${patient.hospitalRegNo})"
        )

        return Resource.Success(Unit)
    }

    override suspend fun updatePatientStatus(
        patientId: String,
        newStatus: PatientStatus,
        reason: String
    ): Resource<Unit> {
        val currentUser = sessionManager.currentUser.value
        if (currentUser == null || !currentUser.hasPermission(Permission.ARCHIVE_PATIENT)) {
            auditRepository.recordAction(
                action = AuditAction.UNAUTHORIZED_ACCESS_ATTEMPT,
                entityName = "Patient",
                entityId = patientId,
                details = "Attempted patient status change without ARCHIVE_PATIENT permission",
                result = "BLOCKED"
            )
            return Resource.Error("Unauthorized: Only hospital administrators can archive or change patient record status")
        }

        val now = System.currentTimeMillis()
        patientDao.updatePatientStatus(patientId, newStatus.name, now, currentUser.username)

        auditRepository.recordAction(
            action = if (newStatus == PatientStatus.ARCHIVED) AuditAction.PATIENT_ARCHIVED else AuditAction.PATIENT_UPDATED,
            entityName = "Patient",
            entityId = patientId,
            details = "Status changed to ${newStatus.name}. Reason: $reason"
        )

        return Resource.Success(Unit)
    }

    override suspend fun generateHospitalRegNo(): String {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val latestRegNo = patientDao.getLatestHospitalRegNo()

        var nextSequence = 1
        if (latestRegNo != null && latestRegNo.startsWith("CF-$currentYear-")) {
            val suffix = latestRegNo.removePrefix("CF-$currentYear-")
            val parsedSeq = suffix.toIntOrNull()
            if (parsedSeq != null) {
                nextSequence = parsedSeq + 1
            }
        } else {
            val count = database.userDao().getUserCount() + 10 // safe fallback offset
            nextSequence = count
        }

        return "CF-$currentYear-${String.format("%06d", nextSequence)}"
    }

    override suspend fun checkInPatientToQueue(
        patientId: String,
        departmentId: String,
        doctorId: String?,
        priority: Int
    ): Resource<String> {
        val currentUser = sessionManager.currentUser.value
        if (currentUser == null || !currentUser.hasPermission(Permission.MANAGE_QUEUE)) {
            return Resource.Error("Unauthorized to issue queue tickets")
        }

        val patient = patientDao.getPatientById(patientId)
            ?: return Resource.Error("Patient not found")

        val prefix = when (departmentId) {
            "dept_cardio" -> "C"
            "dept_ped" -> "P"
            "dept_emerg" -> "E"
            else -> "A"
        }
        val ticketNo = "$prefix-${(100..999).random()}"
        val ticket = QueueTicketEntity(
            id = UUID.randomUUID().toString(),
            ticketNumber = ticketNo,
            patientId = patient.id,
            departmentId = departmentId,
            doctorId = doctorId,
            priority = priority,
            status = "WAITING",
            estimatedWaitMinutes = if (priority > 0) 5 else 15,
            createdAt = System.currentTimeMillis()
        )

        queueDao.insertTicket(ticket)

        auditRepository.recordAction(
            action = AuditAction.QUEUE_TICKET_ISSUED,
            entityName = "QueueTicket",
            entityId = ticket.id,
            details = "Issued ticket $ticketNo for patient ${patient.fullName} (${patient.hospitalRegNo}) in dept $departmentId"
        )

        return Resource.Success(ticketNo)
    }

    private fun PatientEntity.toDomain(): Patient {
        val parsedStatus = try {
            PatientStatus.valueOf(status)
        } catch (e: Exception) {
            PatientStatus.ACTIVE
        }

        return Patient(
            id = id,
            hospitalRegNo = hospitalRegNo,
            fullName = fullName,
            dob = dob,
            gender = gender,
            nationalIdOrPassport = nationalIdOrPassport,
            phone = phone,
            email = email,
            address = address,
            preferredLanguage = preferredLanguage,
            bloodGroup = bloodGroup,
            allergies = allergies,
            existingConditions = existingConditions,
            emergencyContactName = emergencyContactName,
            emergencyContactRelationship = emergencyContactRelationship,
            emergencyContactPhone = emergencyContactPhone,
            emergencyContactSecondaryPhone = emergencyContactSecondaryPhone,
            registeredDate = registeredDate,
            status = parsedStatus,
            createdAt = createdAt,
            updatedAt = updatedAt,
            createdBy = createdBy,
            updatedBy = updatedBy
        )
    }
}
