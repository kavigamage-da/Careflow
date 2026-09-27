package com.example.data.repository

import com.example.core.util.Resource
import com.example.data.local.entity.ClinicalEncounterEntity
import com.example.data.local.entity.LabOrderEntity
import com.example.data.local.entity.PrescriptionEntity
import com.example.data.local.entity.VitalSignEntity
import kotlinx.coroutines.flow.Flow

interface ClinicalRepository {
    // Vitals & Nurse Triage
    fun getPatientVitals(patientId: String): Flow<List<VitalSignEntity>>
    fun getLatestVitals(patientId: String): Flow<VitalSignEntity?>

    suspend fun recordVitals(
        patientId: String,
        temperatureCelsius: Float?,
        systolicBp: Int?,
        diastolicBp: Int?,
        pulseRateBpm: Int?,
        respiratoryRateBpm: Int?,
        oxygenSaturationPercent: Int?,
        weightKg: Float?,
        heightCm: Float?,
        painScore: Int? = 0,
        urgentAttentionFlag: Boolean = false,
        triageNotes: String = ""
    ): Resource<VitalSignEntity>

    // Encounters & Consultations
    fun getPatientEncounters(patientId: String): Flow<List<ClinicalEncounterEntity>>
    fun getAllEncounters(): Flow<List<ClinicalEncounterEntity>>
    suspend fun getEncounterById(encounterId: String): ClinicalEncounterEntity?

    suspend fun recordEncounter(
        patientId: String,
        doctorId: String?,
        chiefComplaint: String,
        historyOfPresentIllness: String,
        examinationNotes: String,
        assessment: String,
        clinicianDiagnosis: String,
        treatmentPlan: String,
        followUpInstructions: String = "",
        status: String = "FINALIZED"
    ): Resource<ClinicalEncounterEntity>

    // Clinical Orders (Prescriptions & Labs)
    suspend fun issuePrescription(
        patientId: String,
        doctorId: String?,
        encounterId: String?,
        itemsJson: String,
        instructions: String
    ): Resource<PrescriptionEntity>

    suspend fun orderLabTest(
        patientId: String,
        doctorId: String?,
        testName: String,
        priority: String = "ROUTINE",
        sampleType: String = "BLOOD"
    ): Resource<LabOrderEntity>

    fun getPatientPrescriptions(patientId: String): Flow<List<PrescriptionEntity>>
    fun getPatientLabOrders(patientId: String): Flow<List<LabOrderEntity>>
}
