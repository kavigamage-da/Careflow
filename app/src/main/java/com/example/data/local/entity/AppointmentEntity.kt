package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DoctorEntity::class,
            parentColumns = ["id"],
            childColumns = ["doctor_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("patient_id"),
        Index("doctor_id"),
        Index("date_time"),
        Index("status")
    ]
)
data class AppointmentEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "patient_id")
    val patientId: String,

    @ColumnInfo(name = "doctor_id")
    val doctorId: String?,

    @ColumnInfo(name = "department_id")
    val departmentId: String?,

    @ColumnInfo(name = "date_time")
    val dateTime: Long,

    @ColumnInfo(name = "appointment_type")
    val appointmentType: String, // "CONSULTATION", "FOLLOW_UP", "ROUTINE_CHECK"

    @ColumnInfo(name = "status")
    val status: String, // "REQUESTED", "CONFIRMED", "CHECKED_IN", "COMPLETED", "CANCELLED", "NO_SHOW"

    @ColumnInfo(name = "reason")
    val reason: String,

    @ColumnInfo(name = "notes")
    val notes: String = ""
)
