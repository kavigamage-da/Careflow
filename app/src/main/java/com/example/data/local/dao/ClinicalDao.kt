package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ClinicalEncounterEntity
import com.example.data.local.entity.VitalSignEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClinicalDao {
    @Query("SELECT * FROM vital_signs WHERE patient_id = :patientId ORDER BY timestamp DESC")
    fun getVitalsForPatient(patientId: String): Flow<List<VitalSignEntity>>

    @Query("SELECT * FROM vital_signs WHERE patient_id = :patientId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestVitalsForPatient(patientId: String): Flow<VitalSignEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVitals(vitals: VitalSignEntity)

    @Query("SELECT * FROM clinical_encounters WHERE patient_id = :patientId ORDER BY timestamp DESC")
    fun getEncountersForPatient(patientId: String): Flow<List<ClinicalEncounterEntity>>

    @Query("SELECT * FROM clinical_encounters WHERE id = :id LIMIT 1")
    suspend fun getEncounterById(id: String): ClinicalEncounterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEncounter(encounter: ClinicalEncounterEntity)

    @Query("SELECT * FROM clinical_encounters ORDER BY timestamp DESC")
    fun getAllEncounters(): Flow<List<ClinicalEncounterEntity>>

    @Query("SELECT COUNT(*) FROM clinical_encounters WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay")
    fun getTodayConsultationCount(startOfDay: Long, endOfDay: Long): Flow<Int>
}
