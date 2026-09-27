package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY date_time DESC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE date_time >= :startOfDay AND date_time <= :endOfDay ORDER BY date_time ASC")
    fun getAppointmentsForDay(startOfDay: Long, endOfDay: Long): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE doctor_id = :doctorId AND date_time >= :startOfDay AND date_time <= :endOfDay")
    fun getDoctorAppointmentsForDay(doctorId: String, startOfDay: Long, endOfDay: Long): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE patient_id = :patientId ORDER BY date_time DESC")
    fun getAppointmentsForPatient(patientId: String): Flow<List<AppointmentEntity>>

    @Query("SELECT COUNT(*) FROM appointments WHERE date_time >= :startOfDay AND date_time <= :endOfDay")
    fun getTodayAppointmentCount(startOfDay: Long, endOfDay: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointments(appointments: List<AppointmentEntity>)

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)
}
