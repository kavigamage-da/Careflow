package com.example.data.repository

import com.example.core.model.AuditAction
import com.example.core.security.SessionManager
import com.example.core.util.Resource
import com.example.data.local.CareFlowDatabase
import com.example.data.local.entity.LabOrderEntity
import kotlinx.coroutines.flow.Flow

class LabRepositoryImpl(
    private val database: CareFlowDatabase,
    private val sessionManager: SessionManager,
    private val auditRepository: AuditRepository
) : LabRepository {

    private val labDao = database.labDao()

    override fun getAllLabOrders(): Flow<List<LabOrderEntity>> {
        return labDao.getAllLabOrders()
    }

    override fun getPendingLabOrders(): Flow<List<LabOrderEntity>> {
        return labDao.getPendingLabOrders()
    }

    override fun getLabOrdersForPatient(patientId: String): Flow<List<LabOrderEntity>> {
        return labDao.getLabOrdersForPatient(patientId)
    }

    override suspend fun getLabOrderById(id: String): LabOrderEntity? {
        return labDao.getLabOrderById(id)
    }

    override suspend fun collectSample(orderId: String, sampleType: String): Resource<LabOrderEntity> {
        return try {
            val order = labDao.getLabOrderById(orderId)
                ?: return Resource.Error("Lab order not found: $orderId")

            val now = System.currentTimeMillis()
            val updated = order.copy(
                status = "SAMPLE_COLLECTED",
                sampleCollectedAt = now,
                sampleType = sampleType
            )
            labDao.updateLabOrder(updated)

            auditRepository.recordAction(
                action = AuditAction.LAB_RESULT_ENTERED,
                entityName = "LabOrder",
                entityId = orderId,
                details = "Sample collected ($sampleType) for order ${order.testName}"
            )

            Resource.Success(updated)
        } catch (e: Exception) {
            Resource.Error("Failed to collect sample: ${e.message}")
        }
    }

    override suspend fun enterResult(orderId: String, resultJson: String, notes: String?): Resource<LabOrderEntity> {
        return try {
            val order = labDao.getLabOrderById(orderId)
                ?: return Resource.Error("Lab order not found: $orderId")

            val updated = order.copy(
                status = "COMPLETED",
                resultJson = resultJson,
                technicianNotes = notes
            )
            labDao.updateLabOrder(updated)

            auditRepository.recordAction(
                action = AuditAction.LAB_RESULT_ENTERED,
                entityName = "LabOrder",
                entityId = orderId,
                details = "Result entered for test ${order.testName}"
            )

            Resource.Success(updated)
        } catch (e: Exception) {
            Resource.Error("Failed to enter result: ${e.message}")
        }
    }

    override suspend fun verifyResult(orderId: String): Resource<LabOrderEntity> {
        return try {
            val order = labDao.getLabOrderById(orderId)
                ?: return Resource.Error("Lab order not found: $orderId")

            val currentUser = sessionManager.getCurrentUser()
            val userId = currentUser?.id ?: "lab_tech"
            val now = System.currentTimeMillis()

            val updated = order.copy(
                status = "VERIFIED",
                verifiedByUserId = userId,
                verifiedAt = now
            )
            labDao.updateLabOrder(updated)

            auditRepository.recordAction(
                action = AuditAction.LAB_RESULT_VERIFIED,
                entityName = "LabOrder",
                entityId = orderId,
                details = "Lab result verified for ${order.testName} by $userId"
            )

            Resource.Success(updated)
        } catch (e: Exception) {
            Resource.Error("Failed to verify result: ${e.message}")
        }
    }
}
