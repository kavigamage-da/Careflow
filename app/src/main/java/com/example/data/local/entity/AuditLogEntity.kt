package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.core.model.AuditAction
import com.example.core.model.UserRole

@Entity(
    tableName = "audit_logs",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["user_id"]),
        Index(value = ["action"]),
        Index(value = ["entity_name"])
    ]
)
data class AuditLogEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "user_id")
    val userId: String?,

    @ColumnInfo(name = "username")
    val username: String?,

    @ColumnInfo(name = "user_role")
    val userRole: UserRole?,

    @ColumnInfo(name = "action")
    val action: AuditAction,

    @ColumnInfo(name = "entity_name")
    val entityName: String,

    @ColumnInfo(name = "entity_id")
    val entityId: String?,

    @ColumnInfo(name = "details")
    val details: String,

    @ColumnInfo(name = "ip_or_device")
    val ipOrDevice: String,

    @ColumnInfo(name = "result")
    val result: String // "SUCCESS", "FAILURE", "BLOCKED"
)
