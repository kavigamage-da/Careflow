package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "prescriptions",
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
        Index("status")
    ]
)
data class PrescriptionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "encounter_id")
    val encounterId: String?,

    @ColumnInfo(name = "patient_id")
    val patientId: String,

    @ColumnInfo(name = "doctor_id")
    val doctorId: String?,

    @ColumnInfo(name = "status")
    val status: String = "ISSUED", // "DRAFT", "ISSUED", "DISPENSING", "DISPENSED", "CANCELLED"

    @ColumnInfo(name = "items_json")
    val itemsJson: String, // Serialized list of medicines, dosages, frequencies, and durations

    @ColumnInfo(name = "instructions")
    val instructions: String,

    @ColumnInfo(name = "dispensed_by_user_id")
    val dispensedByUserId: String? = null,

    @ColumnInfo(name = "dispensed_at")
    val dispensedAt: Long? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
