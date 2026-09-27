package com.example.data.local

import androidx.room.TypeConverter
import com.example.core.model.AuditAction
import com.example.core.model.UserRole

class Converters {

    @TypeConverter
    fun fromUserRole(role: UserRole?): String? {
        return role?.name
    }

    @TypeConverter
    fun toUserRole(name: String?): UserRole? {
        return name?.let {
            try {
                UserRole.valueOf(it)
            } catch (e: Exception) {
                UserRole.PATIENT
            }
        }
    }

    @TypeConverter
    fun fromAuditAction(action: AuditAction?): String? {
        return action?.name
    }

    @TypeConverter
    fun toAuditAction(name: String?): AuditAction? {
        return name?.let {
            try {
                AuditAction.valueOf(it)
            } catch (e: Exception) {
                AuditAction.LOGIN_SUCCESS
            }
        }
    }
}
