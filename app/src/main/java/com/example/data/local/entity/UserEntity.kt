package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.core.model.UserRole

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["email"], unique = false),
        Index(value = ["role"])
    ]
)
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "password_hash")
    val passwordHash: String,

    @ColumnInfo(name = "salt")
    val salt: String,

    @ColumnInfo(name = "full_name")
    val fullName: String,

    @ColumnInfo(name = "role")
    val role: UserRole,

    @ColumnInfo(name = "department_id")
    val departmentId: String? = null,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "phone")
    val phone: String,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "failed_login_attempts")
    val failedLoginAttempts: Int = 0,

    @ColumnInfo(name = "locked_until")
    val lockedUntil: Long? = null,

    @ColumnInfo(name = "force_password_change")
    val forcePasswordChange: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_login_at")
    val lastLoginAt: Long? = null
)
