package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.LabOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LabDao {
    @Query("SELECT * FROM lab_orders ORDER BY ordered_at DESC")
    fun getAllLabOrders(): Flow<List<LabOrderEntity>>

    @Query("SELECT * FROM lab_orders WHERE status IN ('REQUESTED', 'SAMPLE_COLLECTED', 'PROCESSING') ORDER BY ordered_at ASC")
    fun getPendingLabOrders(): Flow<List<LabOrderEntity>>

    @Query("SELECT * FROM lab_orders WHERE patient_id = :patientId ORDER BY ordered_at DESC")
    fun getLabOrdersForPatient(patientId: String): Flow<List<LabOrderEntity>>

    @Query("SELECT COUNT(*) FROM lab_orders WHERE status IN ('REQUESTED', 'SAMPLE_COLLECTED', 'PROCESSING')")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT * FROM lab_orders WHERE id = :id LIMIT 1")
    suspend fun getLabOrderById(id: String): LabOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabOrder(order: LabOrderEntity)

    @Update
    suspend fun updateLabOrder(order: LabOrderEntity)
}
