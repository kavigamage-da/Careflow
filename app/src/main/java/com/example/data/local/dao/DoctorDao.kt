package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.DoctorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoctorDao {
    @Query("SELECT * FROM doctors ORDER BY name ASC")
    fun getAllDoctors(): Flow<List<DoctorEntity>>

    @Query("SELECT * FROM doctors WHERE department_id = :deptId AND is_available = 1")
    fun getDoctorsByDepartment(deptId: String): Flow<List<DoctorEntity>>

    @Query("SELECT * FROM doctors WHERE user_id = :userId LIMIT 1")
    suspend fun getDoctorByUserId(userId: String): DoctorEntity?

    @Query("SELECT * FROM doctors WHERE id = :id LIMIT 1")
    suspend fun getDoctorById(id: String): DoctorEntity?

    @Query("SELECT COUNT(*) FROM doctors")
    suspend fun getDoctorCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctors(doctors: List<DoctorEntity>)
}
