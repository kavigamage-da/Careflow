package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.QueueTicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueDao {
    @Query("SELECT * FROM queue_tickets WHERE status IN ('WAITING', 'CALLED', 'IN_CONSULTATION') ORDER BY priority DESC, check_in_time ASC")
    fun getActiveQueue(): Flow<List<QueueTicketEntity>>

    @Query("SELECT * FROM queue_tickets WHERE department_id = :deptId AND status IN ('WAITING', 'CALLED', 'IN_CONSULTATION') ORDER BY priority DESC, check_in_time ASC")
    fun getQueueByDepartment(deptId: String): Flow<List<QueueTicketEntity>>

    @Query("SELECT * FROM queue_tickets WHERE department_id = :deptId AND status = 'WAITING' ORDER BY priority DESC, check_in_time ASC")
    fun getWaitingTicketsByDepartment(deptId: String): Flow<List<QueueTicketEntity>>

    @Query("SELECT * FROM queue_tickets WHERE doctor_id = :doctorId AND status IN ('WAITING', 'CALLED', 'IN_CONSULTATION') ORDER BY priority DESC, check_in_time ASC")
    fun getQueueByDoctor(doctorId: String): Flow<List<QueueTicketEntity>>

    @Query("SELECT COUNT(*) FROM queue_tickets WHERE status = 'WAITING'")
    fun getWaitingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM queue_tickets WHERE department_id = :deptId AND status = 'WAITING'")
    fun getWaitingCountByDepartment(deptId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM queue_tickets WHERE status = 'IN_CONSULTATION'")
    fun getInConsultationCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM queue_tickets WHERE department_id = :deptId AND status = 'CALLED'")
    fun getCalledCountByDepartment(deptId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM queue_tickets WHERE department_id = :deptId AND status = 'IN_CONSULTATION'")
    fun getInConsultationCountByDepartment(deptId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM queue_tickets WHERE department_id = :deptId AND status = 'COMPLETED' AND completed_at >= :startOfDay")
    fun getCompletedTodayCountByDepartment(deptId: String, startOfDay: Long): Flow<Int>

    @Query("SELECT * FROM queue_tickets WHERE id = :id LIMIT 1")
    suspend fun getTicketById(id: String): QueueTicketEntity?

    @Query("SELECT * FROM queue_tickets WHERE id = :id LIMIT 1")
    fun getTicketByIdFlow(id: String): Flow<QueueTicketEntity?>

    @Query("SELECT * FROM queue_tickets WHERE ticket_number = :ticketNumber LIMIT 1")
    suspend fun getTicketByNumber(ticketNumber: String): QueueTicketEntity?

    @Query("SELECT * FROM queue_tickets WHERE patient_id = :patientId AND status IN ('WAITING', 'CALLED', 'IN_CONSULTATION') ORDER BY created_at DESC LIMIT 1")
    suspend fun getActiveTicketForPatient(patientId: String): QueueTicketEntity?

    @Query("SELECT * FROM queue_tickets WHERE patient_id = :patientId AND status IN ('WAITING', 'CALLED', 'IN_CONSULTATION') ORDER BY created_at DESC LIMIT 1")
    fun getActiveTicketForPatientFlow(patientId: String): Flow<QueueTicketEntity?>

    @Query("SELECT * FROM queue_tickets WHERE appointment_id = :appointmentId LIMIT 1")
    suspend fun getTicketByAppointmentId(appointmentId: String): QueueTicketEntity?

    @Query("SELECT * FROM queue_tickets WHERE patient_id = :patientId ORDER BY created_at DESC")
    fun getPatientQueueHistory(patientId: String): Flow<List<QueueTicketEntity>>

    @Query("""
        SELECT * FROM queue_tickets 
        WHERE (:deptId IS NULL OR department_id = :deptId)
          AND created_at >= :startDate AND created_at <= :endDate
        ORDER BY created_at DESC
    """)
    fun getQueueHistory(deptId: String?, startDate: Long, endDate: Long): Flow<List<QueueTicketEntity>>

    @Query("""
        SELECT * FROM queue_tickets 
        WHERE department_id = :deptId 
          AND status = 'WAITING' 
          AND (:doctorId IS NULL OR doctor_id IS NULL OR doctor_id = :doctorId)
        ORDER BY priority DESC, check_in_time ASC 
        LIMIT 1
    """)
    suspend fun getNextWaitingTicket(deptId: String, doctorId: String? = null): QueueTicketEntity?

    @Query("""
        SELECT COUNT(*) FROM queue_tickets 
        WHERE department_id = :deptId 
          AND status = 'WAITING' 
          AND ((priority > :priority) OR (priority = :priority AND check_in_time < :checkInTime))
    """)
    suspend fun getWaitingPatientsAheadCount(deptId: String, priority: Int, checkInTime: Long): Int

    @Query("""
        SELECT COUNT(*) FROM queue_tickets 
        WHERE department_id = :deptId 
          AND created_at >= :startOfDay
    """)
    suspend fun getDailyTicketCountForDepartment(deptId: String, startOfDay: Long): Int

    @Query("""
        SELECT * FROM queue_tickets 
        WHERE (:deptId IS NULL OR department_id = :deptId)
          AND status IN ('CALLED', 'IN_CONSULTATION')
        ORDER BY called_at DESC
    """)
    fun getServingTickets(deptId: String? = null): Flow<List<QueueTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: QueueTicketEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickets(tickets: List<QueueTicketEntity>)

    @Query("""
        UPDATE queue_tickets 
        SET status = 'CALLED', called_at = :calledAt, called_by = :calledBy, updated_at = :updatedAt, updated_by = :updatedBy 
        WHERE id = :ticketId AND status = 'WAITING'
    """)
    suspend fun callTicketIfWaiting(
        ticketId: String,
        calledBy: String,
        calledAt: Long,
        updatedAt: Long,
        updatedBy: String
    ): Int

    @Update
    suspend fun updateTicket(ticket: QueueTicketEntity)
}
