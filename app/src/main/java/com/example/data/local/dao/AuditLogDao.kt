package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.model.AuditAction
import com.example.data.local.entity.AuditLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    fun getRecentLogs(limit: Int = 100, offset: Int = 0): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE action = :action ORDER BY timestamp DESC LIMIT :limit")
    fun getLogsByAction(action: AuditAction, limit: Int = 100): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE user_id = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getLogsByUser(userId: String, limit: Int = 100): Flow<List<AuditLogEntity>>

    @Query("SELECT COUNT(*) FROM audit_logs")
    fun getLogCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<AuditLogEntity>)
}
