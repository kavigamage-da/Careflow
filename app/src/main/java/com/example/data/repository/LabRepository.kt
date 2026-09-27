package com.example.data.repository

import com.example.core.util.Resource
import com.example.data.local.entity.LabOrderEntity
import kotlinx.coroutines.flow.Flow

interface LabRepository {
    fun getAllLabOrders(): Flow<List<LabOrderEntity>>
    fun getPendingLabOrders(): Flow<List<LabOrderEntity>>
    fun getLabOrdersForPatient(patientId: String): Flow<List<LabOrderEntity>>
    suspend fun getLabOrderById(id: String): LabOrderEntity?
    suspend fun collectSample(orderId: String, sampleType: String = "BLOOD"): Resource<LabOrderEntity>
    suspend fun enterResult(orderId: String, resultJson: String, notes: String? = null): Resource<LabOrderEntity>
    suspend fun verifyResult(orderId: String): Resource<LabOrderEntity>
}
