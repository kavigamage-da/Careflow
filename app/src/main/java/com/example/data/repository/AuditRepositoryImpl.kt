package com.example.data.repository

import com.example.core.model.AuditAction
import com.example.core.model.AuditLog
import com.example.core.security.SessionManager
import com.example.data.local.dao.AuditLogDao
import com.example.data.local.entity.AuditLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class AuditRepositoryImpl(
    private val auditLogDao: AuditLogDao,
    private val sessionManager: SessionManager
) : AuditRepository {

    override fun getRecentLogs(limit: Int, offset: Int): Flow<List<AuditLog>> {
        return auditLogDao.getRecentLogs(limit, offset).map { entities ->
            entities.map { entity ->
                AuditLog(
                    id = entity.id,
                    timestamp = entity.timestamp,
                    userId = entity.userId,
                    username = entity.username,
                    userRole = entity.userRole,
                    action = entity.action,
                    entityName = entity.entityName,
                    entityId = entity.entityId,
                    details = entity.details,
                    ipOrDevice = entity.ipOrDevice,
                    result = entity.result
                )
            }
        }
    }

    override fun getLogCount(): Flow<Int> = auditLogDao.getLogCount()

    override suspend fun recordAction(
        action: AuditAction,
        entityName: String,
        entityId: String?,
        details: String,
        result: String
    ) {
        val currentUser = sessionManager.currentUser.value
        val logEntity = AuditLogEntity(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            userId = currentUser?.id,
            username = currentUser?.username,
            userRole = currentUser?.role,
            action = action,
            entityName = entityName,
            entityId = entityId,
            details = details,
            ipOrDevice = "Android Hospital Station",
            result = result
        )
        auditLogDao.insertLog(logEntity)
    }
}
