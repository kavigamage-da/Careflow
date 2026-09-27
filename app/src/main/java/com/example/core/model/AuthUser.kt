package com.example.core.model

data class AuthUser(
    val id: String,
    val username: String,
    val fullName: String,
    val role: UserRole,
    val departmentId: String?,
    val email: String,
    val phone: String,
    val lastLoginAt: Long?,
    val forcePasswordChange: Boolean = false,
    val sessionToken: String = ""
) {
    fun hasPermission(permission: Permission): Boolean {
        return RolePermissionMatrix.hasPermission(role, permission)
    }
}
