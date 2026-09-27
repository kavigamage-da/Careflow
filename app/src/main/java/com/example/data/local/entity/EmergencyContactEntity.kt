package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "emergency_contacts",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("patient_id")
    ]
)
data class EmergencyContactEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "patient_id")
    val patientId: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "relationship")
    val relationship: String,

    @ColumnInfo(name = "phone")
    val phone: String,

    @ColumnInfo(name = "secondary_phone")
    val secondaryPhone: String = "",

    @ColumnInfo(name = "address")
    val address: String = "",

    @ColumnInfo(name = "is_primary")
    val isPrimary: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
