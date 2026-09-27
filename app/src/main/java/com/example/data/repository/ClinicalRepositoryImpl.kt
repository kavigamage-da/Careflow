package com.example.data.repository

import com.example.core.model.AuditAction
import com.example.core.security.SessionManager
import com.example.core.util.Resource
import com.example.data.local.CareFlowDatabase
import com.example.data.local.entity.ClinicalEncounterEntity
import com.example.data.local.entity.LabOrderEntity
import com.example.data.local.entity.PrescriptionEntity
import com.example.data.local.entity.VitalSignEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ClinicalRepositoryImpl(
    private val database: CareFlowDatabase,
    private val sessionManager: SessionManager,
    private val auditRepository: AuditRepository
) : ClinicalRepository {

    private val clinicalDao = database.clinicalDao()
    private val prescriptionDao = database.prescriptionDao()
    private val labDao = database.labDao()

    override fun getPatientVitals(patientId: String): Flow<List<VitalSignEntity>> {
        return clinicalDao.getVitalsForPatient(patientId)
    }

    override fun getLatestVitals(patientId: String): Flow<VitalSignEntity?> {
        return clinicalDao.getLatestVitalsForPatient(patientId)
    }

    override suspend fun recordVitals(
        patientId: String,
        temperatureCelsius: Float?,
        systolicBp: Int?,
        diastolicBp: Int?,
        pulseRateBpm: Int?,
        respiratoryRateBpm: Int?,
        oxygenSaturationPercent: Int?,
        weightKg: Float?,
        heightCm: Float?,
        painScore: Int?,
        urgentAttentionFlag: Boolean,
        triageNotes: String
    ): Resource<VitalSignEntity> {
        return try {
            val currentUser = sessionManager.getCurrentUser()
            val userId = currentUser?.id ?: "nurse_staff"
            val now = System.currentTimeMillis()

            // Safety input validation: Physiological range bounds
            if (systolicBp != null && (systolicBp < 40 || systolicBp > 300)) {
                return Resource.Error("Systolic BP must be between 40 and 300 mmHg")
            }
            if (diastolicBp != null && (diastolicBp < 20 || diastolicBp > 200)) {
                return Resource.Error("Diastolic BP must be between 20 and 200 mmHg")
            }
            if (pulseRateBpm != null && (pulseRateBpm < 20 || pulseRateBpm > 250)) {
                return Resource.Error("Pulse rate must be between 20 and 250 bpm")
            }
            if (oxygenSaturationPercent != null && (oxygenSaturationPercent < 50 || oxygenSaturationPercent > 100)) {
                return Resource.Error("SpO2 must be between 50% and 100%")
            }
            if (temperatureCelsius != null && (temperatureCelsius < 30.0f || temperatureCelsius > 45.0f)) {
                return Resource.Error("Temperature must be between 30.0°C and 45.0°C")
            }

            val vitals = VitalSignEntity(
                id = UUID.randomUUID().toString(),
                patientId = patientId,
                recordedByUserId = userId,
                timestamp = now,
                temperatureCelsius = temperatureCelsius,
                systolicBp = systolicBp,
                diastolicBp = diastolicBp,
                pulseRateBpm = pulseRateBpm,
                respiratoryRateBpm = respiratoryRateBpm,
                oxygenSaturationPercent = oxygenSaturationPercent,
                weightKg = weightKg,
                heightCm = heightCm,
                painScore = painScore ?: 0,
                urgentAttentionFlag = urgentAttentionFlag,
                triageNotes = triageNotes
            )

            clinicalDao.insertVitals(vitals)

            auditRepository.recordAction(
                action = AuditAction.VITALS_RECORDED,
                entityName = "VitalSign",
                entityId = vitals.id,
                details = "Vitals recorded for patient $patientId by $userId. Urgent flag: $urgentAttentionFlag"
            )

            Resource.Success(vitals)
        } catch (e: Exception) {
            Resource.Error("Failed to record vitals: ${e.message}")
        }
    }

    override fun getPatientEncounters(patientId: String): Flow<List<ClinicalEncounterEntity>> {
        return clinicalDao.getEncountersForPatient(patientId)
    }

    override fun getAllEncounters(): Flow<List<ClinicalEncounterEntity>> {
        return clinicalDao.getAllEncounters()
    }

    override suspend fun getEncounterById(encounterId: String): ClinicalEncounterEntity? {
        return clinicalDao.getEncounterById(encounterId)
    }

    override suspend fun recordEncounter(
        patientId: String,
        doctorId: String?,
        chiefComplaint: String,
        historyOfPresentIllness: String,
        examinationNotes: String,
        assessment: String,
        clinicianDiagnosis: String,
        treatmentPlan: String,
        followUpInstructions: String,
        status: String
    ): Resource<ClinicalEncounterEntity> {
        return try {
            val currentUser = sessionManager.getCurrentUser()
            val effectiveDoctorId = doctorId ?: currentUser?.id ?: "doc_staff"
            val now = System.currentTimeMillis()

            if (chiefComplaint.isBlank()) {
                return Resource.Error("Chief complaint is required")
            }
            if (clinicianDiagnosis.isBlank()) {
                return Resource.Error("Clinician diagnosis documentation is required")
            }

            val encounter = ClinicalEncounterEntity(
                id = UUID.randomUUID().toString(),
                patientId = patientId,
                doctorId = effectiveDoctorId,
                timestamp = now,
                chiefComplaint = chiefComplaint.trim(),
                historyOfPresentIllness = historyOfPresentIllness.trim(),
                examinationNotes = examinationNotes.trim(),
                assessment = assessment.trim(),
                clinicianDiagnosis = clinicianDiagnosis.trim(),
                treatmentPlan = treatmentPlan.trim(),
                followUpInstructions = followUpInstructions.trim(),
                status = status
            )

            clinicalDao.insertEncounter(encounter)

            auditRepository.recordAction(
                action = AuditAction.CONSULTATION_CREATED,
                entityName = "ClinicalEncounter",
                entityId = encounter.id,
                details = "Consultation encounter documented for patient $patientId by $effectiveDoctorId. Diagnosis: $clinicianDiagnosis"
            )

            Resource.Success(encounter)
        } catch (e: Exception) {
            Resource.Error("Failed to save clinical encounter: ${e.message}")
        }
    }

    override suspend fun issuePrescription(
        patientId: String,
        doctorId: String?,
        encounterId: String?,
        itemsJson: String,
        instructions: String
    ): Resource<PrescriptionEntity> {
        return try {
            val currentUser = sessionManager.getCurrentUser()
            val docId = doctorId ?: currentUser?.id ?: "doc_staff"
            val now = System.currentTimeMillis()

            val prescription = PrescriptionEntity(
                id = UUID.randomUUID().toString(),
                encounterId = encounterId,
                patientId = patientId,
                doctorId = docId,
                status = "ISSUED",
                itemsJson = itemsJson,
                instructions = instructions,
                createdAt = now
            )

            prescriptionDao.insertPrescription(prescription)

            auditRepository.recordAction(
                action = AuditAction.PRESCRIPTION_ISSUED,
                entityName = "Prescription",
                entityId = prescription.id,
                details = "Prescription issued for patient $patientId by $docId"
            )

            Resource.Success(prescription)
        } catch (e: Exception) {
            Resource.Error("Failed to issue prescription: ${e.message}")
        }
    }

    override suspend fun orderLabTest(
        patientId: String,
        doctorId: String?,
        testName: String,
        priority: String,
        sampleType: String
    ): Resource<LabOrderEntity> {
        return try {
            val currentUser = sessionManager.getCurrentUser()
            val docId = doctorId ?: currentUser?.id ?: "doc_staff"
            val now = System.currentTimeMillis()

            val labOrder = LabOrderEntity(
                id = UUID.randomUUID().toString(),
                patientId = patientId,
                doctorId = docId,
                testName = testName,
                priority = priority,
                status = "REQUESTED",
                orderedAt = now,
                sampleType = sampleType
            )

            labDao.insertLabOrder(labOrder)

            auditRepository.recordAction(
                action = AuditAction.LAB_ORDER_CREATED,
                entityName = "LabOrder",
                entityId = labOrder.id,
                details = "Lab test $testName ordered for patient $patientId ($priority)"
            )

            Resource.Success(labOrder)
        } catch (e: Exception) {
            Resource.Error("Failed to order lab test: ${e.message}")
        }
    }

    override fun getPatientPrescriptions(patientId: String): Flow<List<PrescriptionEntity>> {
        return prescriptionDao.getPrescriptionsForPatient(patientId)
    }

    override fun getPatientLabOrders(patientId: String): Flow<List<LabOrderEntity>> {
        return labDao.getLabOrdersForPatient(patientId)
    }
}
