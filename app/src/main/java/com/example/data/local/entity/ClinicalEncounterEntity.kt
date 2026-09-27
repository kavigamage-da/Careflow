package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clinical_encounters",
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
        Index("timestamp")
    ]
)
data class ClinicalEncounterEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "patient_id")
    val patientId: String,

    @ColumnInfo(name = "doctor_id")
    val doctorId: String?,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "chief_complaint")
    val chiefComplaint: String,

    @ColumnInfo(name = "history_of_present_illness")
    val historyOfPresentIllness: String,

    @ColumnInfo(name = "examination_notes")
    val examinationNotes: String,

    @ColumnInfo(name = "assessment")
    val assessment: String,

    @ColumnInfo(name = "clinician_diagnosis")
    val clinicianDiagnosis: String,

    @ColumnInfo(name = "treatment_plan")
    val treatmentPlan: String,

    @ColumnInfo(name = "follow_up_instructions")
    val followUpInstructions: String = "",

    @ColumnInfo(name = "status")
    val status: String = "FINALIZED" // "DRAFT", "FINALIZED"
)
