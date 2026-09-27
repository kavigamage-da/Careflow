package com.example.data.repository

import com.example.core.model.AuthUser
import com.example.core.util.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<AuthUser?>
    suspend fun login(username: String, password: String): Resource<AuthUser>
    suspend fun logout(): Resource<Unit>
    suspend fun changePassword(oldPass: String, newPass: String): Resource<Unit>
    suspend fun requestPasswordReset(usernameOrEmail: String): Resource<String>
    suspend fun quickLoginRole(username: String): Resource<AuthUser>
}
