package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lab_orders",
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
data class LabOrderEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "patient_id")
    val patientId: String,

    @ColumnInfo(name = "doctor_id")
    val doctorId: String?,

    @ColumnInfo(name = "test_name")
    val testName: String,

    @ColumnInfo(name = "priority")
    val priority: String = "ROUTINE", // "ROUTINE", "STAT", "URGENT"

    @ColumnInfo(name = "status")
    val status: String = "REQUESTED", // "REQUESTED", "SAMPLE_COLLECTED", "PROCESSING", "COMPLETED", "VERIFIED", "CANCELLED"

    @ColumnInfo(name = "ordered_at")
    val orderedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "sample_collected_at")
    val sampleCollectedAt: Long? = null,

    @ColumnInfo(name = "sample_type")
    val sampleType: String = "BLOOD",

    @ColumnInfo(name = "result_json")
    val resultJson: String? = null,

    @ColumnInfo(name = "technician_notes")
    val technicianNotes: String? = null,

    @ColumnInfo(name = "verified_by_user_id")
    val verifiedByUserId: String? = null,

    @ColumnInfo(name = "verified_at")
    val verifiedAt: Long? = null
)
