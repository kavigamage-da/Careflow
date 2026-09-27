package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vital_signs",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("patient_id"),
        Index("timestamp")
    ]
)
data class VitalSignEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "patient_id")
    val patientId: String,

    @ColumnInfo(name = "recorded_by_user_id")
    val recordedByUserId: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "temperature_celsius")
    val temperatureCelsius: Float?,

    @ColumnInfo(name = "systolic_bp")
    val systolicBp: Int?,

    @ColumnInfo(name = "diastolic_bp")
    val diastolicBp: Int?,

    @ColumnInfo(name = "pulse_rate_bpm")
    val pulseRateBpm: Int?,

    @ColumnInfo(name = "respiratory_rate_bpm")
    val respiratoryRateBpm: Int?,

    @ColumnInfo(name = "oxygen_saturation_percent")
    val oxygenSaturationPercent: Int?,

    @ColumnInfo(name = "weight_kg")
    val weightKg: Float?,

    @ColumnInfo(name = "height_cm")
    val heightCm: Float?,

    @ColumnInfo(name = "pain_score")
    val painScore: Int? = 0, // 0-10

    @ColumnInfo(name = "urgent_attention_flag")
    val urgentAttentionFlag: Boolean = false,

    @ColumnInfo(name = "triage_notes")
    val triageNotes: String = ""
)
