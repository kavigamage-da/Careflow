package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "queue_tickets",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["ticket_number"], unique = true),
        Index("patient_id"),
        Index("department_id"),
        Index("doctor_id"),
        Index("appointment_id"),
        Index("status"),
        Index("priority"),
        Index("check_in_time")
    ]
)
data class QueueTicketEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "ticket_number")
    val ticketNumber: String,

    @ColumnInfo(name = "patient_id")
    val patientId: String,

    @ColumnInfo(name = "department_id")
    val departmentId: String,

    @ColumnInfo(name = "doctor_id")
    val doctorId: String? = null,

    @ColumnInfo(name = "appointment_id")
    val appointmentId: String? = null,

    @ColumnInfo(name = "priority")
    val priority: Int = 0, // 0 = Standard, 1 = Fast Track, 2 = Urgent, 3 = Emergency

    @ColumnInfo(name = "priority_assigned_by")
    val priorityAssignedBy: String? = null,

    @ColumnInfo(name = "status")
    val status: String, // "WAITING", "CALLED", "IN_CONSULTATION", "COMPLETED", "SKIPPED", "CANCELLED"

    @ColumnInfo(name = "check_in_time")
    val checkInTime: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "called_at")
    val calledAt: Long? = null,

    @ColumnInfo(name = "consultation_start_time")
    val consultationStartTime: Long? = null,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,

    @ColumnInfo(name = "skipped_time")
    val skippedTime: Long? = null,

    @ColumnInfo(name = "cancelled_time")
    val cancelledTime: Long? = null,

    @ColumnInfo(name = "estimated_wait_minutes")
    val estimatedWaitMinutes: Int? = 15,

    @ColumnInfo(name = "recall_count")
    val recallCount: Int = 0,

    @ColumnInfo(name = "called_by")
    val calledBy: String? = null,

    @ColumnInfo(name = "consultation_by")
    val consultationBy: String? = null,

    @ColumnInfo(name = "completed_by")
    val completedBy: String? = null,

    @ColumnInfo(name = "transfer_source_department_id")
    val transferSourceDepartmentId: String? = null,

    @ColumnInfo(name = "transfer_reason")
    val transferReason: String? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "created_by")
    val createdBy: String = "system",

    @ColumnInfo(name = "updated_by")
    val updatedBy: String = "system"
) {
    val queueNumber: String get() = ticketNumber
}
