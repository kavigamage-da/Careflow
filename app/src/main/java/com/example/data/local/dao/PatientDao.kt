package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PatientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {

    @Query("SELECT * FROM patients ORDER BY registered_date DESC")
    fun getAllPatients(): Flow<List<PatientEntity>>

    @Query("""
        SELECT * FROM patients 
        WHERE (:status = 'ALL' OR status = :status)
        ORDER BY 
            CASE WHEN :sortBy = 'NAME_ASC' THEN full_name END ASC,
            CASE WHEN :sortBy = 'DATE_DESC' THEN registered_date END DESC,
            registered_date DESC
        LIMIT :limit OFFSET :offset
    """)
    fun getPatientsPaged(
        status: String = "ALL",
        sortBy: String = "DATE_DESC",
        limit: Int = 100,
        offset: Int = 0
    ): Flow<List<PatientEntity>>

    @Query("""
        SELECT * FROM patients 
        WHERE (
            full_name LIKE '%' || :query || '%' 
            OR hospital_reg_no LIKE '%' || :query || '%' 
            OR phone LIKE '%' || :query || '%'
            OR email LIKE '%' || :query || '%'
            OR national_id_or_passport LIKE '%' || :query || '%'
            OR id = :query
        )
        AND (:status = 'ALL' OR status = :status)
        ORDER BY registered_date DESC
        LIMIT :limit
    """)
    fun searchPatients(
        query: String,
        status: String = "ALL",
        limit: Int = 50
    ): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE id = :id LIMIT 1")
    suspend fun getPatientById(id: String): PatientEntity?

    @Query("SELECT * FROM patients WHERE id = :id LIMIT 1")
    fun observePatientById(id: String): Flow<PatientEntity?>

    @Query("SELECT * FROM patients WHERE hospital_reg_no = :regNo LIMIT 1")
    suspend fun getPatientByRegNo(regNo: String): PatientEntity?

    @Query("SELECT COUNT(*) FROM patients")
    fun getPatientCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM patients WHERE status = :status")
    fun getPatientCountByStatus(status: String): Flow<Int>

    // Potential duplicate detection queries
    @Query("""
        SELECT * FROM patients 
        WHERE (phone = :phone AND :phone != '')
           OR (email = :email AND :email != '')
           OR (national_id_or_passport = :nationalId AND :nationalId != '')
           OR (LOWER(TRIM(full_name)) = LOWER(TRIM(:fullName)) AND dob = :dob)
        LIMIT 5
    """)
    suspend fun findPossibleDuplicates(
        phone: String,
        email: String,
        nationalId: String,
        fullName: String,
        dob: String
    ): List<PatientEntity>

    @Query("SELECT hospital_reg_no FROM patients ORDER BY registered_date DESC LIMIT 1")
    suspend fun getLatestHospitalRegNo(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: PatientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatients(patients: List<PatientEntity>)

    @Update
    suspend fun updatePatient(patient: PatientEntity)

    @Query("UPDATE patients SET status = :status, updated_at = :updatedAt, updated_by = :updatedBy WHERE id = :id")
    suspend fun updatePatientStatus(id: String, status: String, updatedAt: Long, updatedBy: String)
}
