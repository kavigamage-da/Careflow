package com.example.data.repository

import com.example.core.model.AuditAction
import com.example.core.security.SessionManager
import com.example.core.util.Resource
import com.example.data.local.CareFlowDatabase
import com.example.data.local.entity.PrescriptionEntity
import kotlinx.coroutines.flow.Flow

class PharmacyRepositoryImpl(
    private val database: CareFlowDatabase,
    private val sessionManager: SessionManager,
    private val auditRepository: AuditRepository
) : PharmacyRepository {

    private val prescriptionDao = database.prescriptionDao()

    override fun getAllPrescriptions(): Flow<List<PrescriptionEntity>> {
        return prescriptionDao.getAllPrescriptions()
    }

    override fun getPendingPrescriptions(): Flow<List<PrescriptionEntity>> {
        return prescriptionDao.getPendingPrescriptions()
    }

    override fun getPrescriptionsForPatient(patientId: String): Flow<List<PrescriptionEntity>> {
        return prescriptionDao.getPrescriptionsForPatient(patientId)
    }

    override suspend fun getPrescriptionById(id: String): PrescriptionEntity? {
        return prescriptionDao.getPrescriptionById(id)
    }

    override suspend fun markDispensing(prescriptionId: String): Resource<PrescriptionEntity> {
        return try {
            val prescription = prescriptionDao.getPrescriptionById(prescriptionId)
                ?: return Resource.Error("Prescription not found: $prescriptionId")

            val currentUser = sessionManager.getCurrentUser()
            val userId = currentUser?.id ?: "pharmacist"

            val updated = prescription.copy(
                status = "DISPENSING"
            )
            prescriptionDao.updatePrescription(updated)
            Resource.Success(updated)
        } catch (e: Exception) {
            Resource.Error("Failed to update status: ${e.message}")
        }
    }

    override suspend fun dispensePrescription(prescriptionId: String, notes: String?): Resource<PrescriptionEntity> {
        return try {
            val prescription = prescriptionDao.getPrescriptionById(prescriptionId)
                ?: return Resource.Error("Prescription not found: $prescriptionId")

            if (prescription.status == "DISPENSED") {
                return Resource.Error("Prescription has already been dispensed")
            }

            val currentUser = sessionManager.getCurrentUser()
            val userId = currentUser?.id ?: "pharmacist"
            val now = System.currentTimeMillis()

            val updated = prescription.copy(
                status = "DISPENSED",
                dispensedByUserId = userId,
                dispensedAt = now
            )

            prescriptionDao.updatePrescription(updated)

            auditRepository.recordAction(
                action = AuditAction.PRESCRIPTION_DISPENSED,
                entityName = "Prescription",
                entityId = prescription.id,
                details = "Prescription ${prescription.id} dispensed to patient ${prescription.patientId} by $userId"
            )

            Resource.Success(updated)
        } catch (e: Exception) {
            Resource.Error("Failed to dispense prescription: ${e.message}")
        }
    }

    override suspend fun cancelPrescription(prescriptionId: String, reason: String): Resource<PrescriptionEntity> {
        return try {
            val prescription = prescriptionDao.getPrescriptionById(prescriptionId)
                ?: return Resource.Error("Prescription not found: $prescriptionId")

            val updated = prescription.copy(status = "CANCELLED")
            prescriptionDao.updatePrescription(updated)
            Resource.Success(updated)
        } catch (e: Exception) {
            Resource.Error("Failed to cancel prescription: ${e.message}")
        }
    }
}
