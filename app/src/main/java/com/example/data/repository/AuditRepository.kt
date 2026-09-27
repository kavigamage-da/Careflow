package com.example.data.repository

import com.example.core.model.AuditAction
import com.example.core.model.AuditLog
import kotlinx.coroutines.flow.Flow

interface AuditRepository {
    fun getRecentLogs(limit: Int = 100, offset: Int = 0): Flow<List<AuditLog>>
    fun getLogCount(): Flow<Int>
    suspend fun recordAction(
        action: AuditAction,
        entityName: String,
        entityId: String?,
        details: String,
        result: String = "SUCCESS"
    )
}
