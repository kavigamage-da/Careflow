package com.example.data.repository

import com.example.core.util.Resource
import com.example.data.local.entity.PrescriptionEntity
import kotlinx.coroutines.flow.Flow

interface PharmacyRepository {
    fun getAllPrescriptions(): Flow<List<PrescriptionEntity>>
    fun getPendingPrescriptions(): Flow<List<PrescriptionEntity>>
    fun getPrescriptionsForPatient(patientId: String): Flow<List<PrescriptionEntity>>
    suspend fun getPrescriptionById(id: String): PrescriptionEntity?
    suspend fun dispensePrescription(prescriptionId: String, notes: String? = null): Resource<PrescriptionEntity>
    suspend fun markDispensing(prescriptionId: String): Resource<PrescriptionEntity>
    suspend fun cancelPrescription(prescriptionId: String, reason: String): Resource<PrescriptionEntity>
}
