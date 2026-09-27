package com.example.data.repository

import com.example.core.model.DepartmentQueueMetrics
import com.example.core.model.QueuePriority
import com.example.core.model.QueueTicket
import com.example.core.util.Resource
import kotlinx.coroutines.flow.Flow

interface QueueRepository {
    fun getActiveQueue(departmentId: String? = null): Flow<List<QueueTicket>>
    fun getWaitingTickets(departmentId: String? = null): Flow<List<QueueTicket>>
    fun getServingTickets(departmentId: String? = null): Flow<List<QueueTicket>>
    fun getTicketById(id: String): Flow<QueueTicket?>
    fun getPatientActiveTicket(patientId: String): Flow<QueueTicket?>

    suspend fun createTicket(
        patientId: String,
        departmentId: String,
        doctorId: String? = null,
        priority: QueuePriority = QueuePriority.STANDARD,
        appointmentId: String? = null,
        notes: String? = null
    ): Resource<QueueTicket>

    suspend fun callNextPatient(departmentId: String, doctorId: String? = null): Resource<QueueTicket>
    suspend fun recallPatient(ticketId: String): Resource<QueueTicket>
    suspend fun startConsultation(ticketId: String, doctorId: String? = null): Resource<QueueTicket>
    suspend fun completeConsultation(ticketId: String): Resource<QueueTicket>
    suspend fun skipPatient(ticketId: String, reason: String = "No response to call"): Resource<QueueTicket>
    suspend fun cancelTicket(ticketId: String, reason: String): Resource<QueueTicket>
    suspend fun transferDepartment(ticketId: String, targetDepartmentId: String, reason: String): Resource<QueueTicket>
    suspend fun updatePriority(ticketId: String, newPriority: QueuePriority, reason: String): Resource<QueueTicket>
    fun getDepartmentMetrics(departmentId: String): Flow<DepartmentQueueMetrics>
}
