package com.example.data.repository

import com.example.core.model.Patient
import com.example.core.model.PatientStatus
import com.example.core.model.PatientTimelineEvent
import com.example.core.util.Resource
import kotlinx.coroutines.flow.Flow

interface PatientRepository {
    fun getAllPatients(statusFilter: String = "ALL", sortBy: String = "DATE_DESC"): Flow<List<Patient>>
    fun searchPatients(query: String, statusFilter: String = "ALL"): Flow<List<Patient>>
    fun getPatientById(id: String): Flow<Patient?>
    fun getPatientTimeline(patientId: String): Flow<List<PatientTimelineEvent>>

    suspend fun checkPossibleDuplicates(
        phone: String,
        email: String,
        nationalId: String,
        fullName: String,
        dob: String
    ): List<Patient>

    suspend fun registerPatient(patient: Patient): Resource<Patient>
    suspend fun updatePatientDemographics(patient: Patient): Resource<Unit>
    suspend fun updatePatientStatus(patientId: String, newStatus: PatientStatus, reason: String): Resource<Unit>
    suspend fun generateHospitalRegNo(): String
    suspend fun checkInPatientToQueue(patientId: String, departmentId: String, doctorId: String?, priority: Int): Resource<String>
}
