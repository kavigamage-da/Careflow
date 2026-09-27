package com.example.core.model

data class QueueTicket(
    val id: String,
    val ticketNumber: String,
    val patientId: String,
    val patientName: String = "",
    val patientRegNo: String = "",
    val appointmentId: String? = null,
    val departmentId: String,
    val departmentName: String = "",
    val doctorId: String? = null,
    val doctorName: String? = null,
    val priority: QueuePriority = QueuePriority.STANDARD,
    val priorityAssignedBy: String? = null,
    val status: QueueStatus = QueueStatus.WAITING,
    val checkInTime: Long,
    val calledTime: Long? = null,
    val consultationStartTime: Long? = null,
    val completedTime: Long? = null,
    val skippedTime: Long? = null,
    val cancelledTime: Long? = null,
    val estimatedWaitMinutes: Int? = null,
    val recallCount: Int = 0,
    val calledBy: String? = null,
    val consultationBy: String? = null,
    val completedBy: String? = null,
    val transferSourceDepartmentId: String? = null,
    val transferReason: String? = null,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val createdBy: String,
    val updatedBy: String
)

data class QueuePositionInfo(
    val ticketId: String,
    val queueNumber: String,
    val position: Int,
    val patientsAhead: Int,
    val currentServingTicketNumber: String?,
    val currentServingDoctorName: String?,
    val estimatedWaitMinutes: Int?,
    val status: QueueStatus,
    val departmentName: String
)

data class DepartmentQueueMetrics(
    val departmentId: String,
    val departmentName: String,
    val waitingCount: Int = 0,
    val calledCount: Int = 0,
    val inConsultationCount: Int = 0,
    val completedTodayCount: Int = 0,
    val currentServingTicketNumber: String? = null,
    val averageWaitMinutes: Int = 15
)

data class QueueDisplayBoardData(
    val departmentId: String?,
    val departmentName: String,
    val currentServingTickets: List<ServingTicketDisplay> = emptyList(),
    val nextInLineNumbers: List<String> = emptyList(),
    val totalWaiting: Int = 0,
    val estimatedAverageWaitMinutes: Int = 15
)

data class ServingTicketDisplay(
    val ticketNumber: String,
    val roomOrCounter: String,
    val doctorName: String?,
    val departmentName: String
)
