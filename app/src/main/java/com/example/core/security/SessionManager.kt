package com.example.core.security

import com.example.core.model.AuthUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager {

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    fun getCurrentUser(): AuthUser? = _currentUser.value

    private var lastActivityTimestamp: Long = System.currentTimeMillis()
    private var sessionTimeoutMinutes: Long = 30L

    fun configureTimeout(minutes: Long) {
        if (minutes > 0) {
            sessionTimeoutMinutes = minutes
        }
    }

    fun startSession(user: AuthUser) {
        _currentUser.value = user
        lastActivityTimestamp = System.currentTimeMillis()
    }

    fun refreshActivity() {
        lastActivityTimestamp = System.currentTimeMillis()
    }

    fun isSessionExpired(): Boolean {
        if (_currentUser.value == null) return false
        val elapsedMillis = System.currentTimeMillis() - lastActivityTimestamp
        val timeoutMillis = sessionTimeoutMinutes * 60 * 1000
        return elapsedMillis > timeoutMillis
    }

    fun terminateSession() {
        _currentUser.value = null
        lastActivityTimestamp = 0L
    }

    fun getRemainingSessionSeconds(): Long {
        if (_currentUser.value == null) return 0L
        val elapsedMillis = System.currentTimeMillis() - lastActivityTimestamp
        val timeoutMillis = sessionTimeoutMinutes * 60 * 1000
        val remaining = (timeoutMillis - elapsedMillis) / 1000
        return if (remaining > 0) remaining else 0
    }
}
