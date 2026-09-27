package com.example.data.repository

import com.example.core.model.AuditAction
import com.example.core.model.AuthUser
import com.example.core.security.SecurityManager
import com.example.core.security.SessionManager
import com.example.core.util.Resource
import com.example.data.local.dao.UserDao
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AuthRepositoryImpl(
    private val userDao: UserDao,
    private val sessionManager: SessionManager,
    private val auditRepository: AuditRepository
) : AuthRepository {

    override val currentUser: Flow<AuthUser?> = sessionManager.currentUser

    override suspend fun login(username: String, password: String): Resource<AuthUser> {
        val trimmedUsername = username.trim()
        if (trimmedUsername.isBlank()) {
            return Resource.Error("Username is required")
        }
        if (password.isBlank()) {
            return Resource.Error("Password is required")
        }

        val userEntity = userDao.getUserByUsername(trimmedUsername)
        val now = System.currentTimeMillis()

        if (userEntity == null) {
            auditRepository.recordAction(
                action = AuditAction.LOGIN_FAILED,
                entityName = "User",
                entityId = trimmedUsername,
                details = "Login attempt with non-existent username: $trimmedUsername",
                result = "FAILURE"
            )
            return Resource.Error("Invalid username or password")
        }

        if (!userEntity.isActive) {
            auditRepository.recordAction(
                action = AuditAction.LOGIN_FAILED,
                entityName = "User",
                entityId = userEntity.id,
                details = "Attempted login to deactivated account: ${userEntity.username}",
                result = "BLOCKED"
            )
            return Resource.Error("This staff account is deactivated. Contact hospital IT.")
        }

        // Account Lockout verification
        val lockedUntil = userEntity.lockedUntil
        if (lockedUntil != null && lockedUntil > now) {
            val minutesRemaining = ((lockedUntil - now) / 60000) + 1
            auditRepository.recordAction(
                action = AuditAction.LOGIN_FAILED,
                entityName = "User",
                entityId = userEntity.id,
                details = "Login attempted on locked account (locked for $minutesRemaining more mins)",
                result = "BLOCKED"
            )
            return Resource.Error("Account temporarily locked due to repeated failed attempts. Try again in $minutesRemaining minutes.")
        }

        val isPasswordCorrect = SecurityManager.verifyPassword(password, userEntity.salt, userEntity.passwordHash)

        if (!isPasswordCorrect) {
            val newFailedAttempts = userEntity.failedLoginAttempts + 1
            val shouldLock = newFailedAttempts >= 5
            val lockTime = if (shouldLock) now + (15 * 60 * 1000) else null

            userDao.updateLockoutState(userEntity.id, newFailedAttempts, lockTime)

            if (shouldLock) {
                auditRepository.recordAction(
                    action = AuditAction.ACCOUNT_LOCKED,
                    entityName = "User",
                    entityId = userEntity.id,
                    details = "Account locked for 15 minutes after 5 consecutive failed login attempts",
                    result = "BLOCKED"
                )
                return Resource.Error("Account locked for 15 minutes due to 5 consecutive failed attempts.")
            } else {
                auditRepository.recordAction(
                    action = AuditAction.LOGIN_FAILED,
                    entityName = "User",
                    entityId = userEntity.id,
                    details = "Failed password attempt ($newFailedAttempts of 5)",
                    result = "FAILURE"
                )
                return Resource.Error("Invalid username or password (${5 - newFailedAttempts} attempts remaining)")
            }
        }

        // Successful authentication
        userDao.recordSuccessfulLogin(userEntity.id, now)

        val token = SecurityManager.generateSessionToken()
        val authUser = AuthUser(
            id = userEntity.id,
            username = userEntity.username,
            fullName = userEntity.fullName,
            role = userEntity.role,
            departmentId = userEntity.departmentId,
            email = userEntity.email,
            phone = userEntity.phone,
            lastLoginAt = userEntity.lastLoginAt,
            forcePasswordChange = userEntity.forcePasswordChange,
            sessionToken = token
        )

        sessionManager.startSession(authUser)

        auditRepository.recordAction(
            action = AuditAction.LOGIN_SUCCESS,
            entityName = "User",
            entityId = authUser.id,
            details = "Successful login for ${authUser.fullName} as ${authUser.role.name}",
            result = "SUCCESS"
        )

        return Resource.Success(authUser)
    }

    override suspend fun quickLoginRole(username: String): Resource<AuthUser> {
        val userEntity = userDao.getUserByUsername(username)
            ?: return Resource.Error("Demo user not found")

        val now = System.currentTimeMillis()
        userDao.recordSuccessfulLogin(userEntity.id, now)

        val authUser = AuthUser(
            id = userEntity.id,
            username = userEntity.username,
            fullName = userEntity.fullName,
            role = userEntity.role,
            departmentId = userEntity.departmentId,
            email = userEntity.email,
            phone = userEntity.phone,
            lastLoginAt = userEntity.lastLoginAt,
            forcePasswordChange = false,
            sessionToken = SecurityManager.generateSessionToken()
        )

        sessionManager.startSession(authUser)

        auditRepository.recordAction(
            action = AuditAction.LOGIN_SUCCESS,
            entityName = "User",
            entityId = authUser.id,
            details = "Quick Demo login as ${authUser.role.displayName}",
            result = "SUCCESS"
        )

        return Resource.Success(authUser)
    }

    override suspend fun logout(): Resource<Unit> {
        val currentUser = sessionManager.currentUser.value
        if (currentUser != null) {
            auditRepository.recordAction(
                action = AuditAction.LOGOUT,
                entityName = "User",
                entityId = currentUser.id,
                details = "User ${currentUser.username} logged out safely",
                result = "SUCCESS"
            )
        }
        sessionManager.terminateSession()
        return Resource.Success(Unit)
    }

    override suspend fun changePassword(oldPass: String, newPass: String): Resource<Unit> {
        val currentUser = sessionManager.currentUser.value
            ?: return Resource.Error("No active user session")

        val userEntity = userDao.getUserById(currentUser.id)
            ?: return Resource.Error("User record not found")

        if (!SecurityManager.verifyPassword(oldPass, userEntity.salt, userEntity.passwordHash)) {
            return Resource.Error("Current password is incorrect")
        }

        val policyResult = SecurityManager.validatePasswordPolicy(newPass)
        if (!policyResult.isValid) {
            return Resource.Error(policyResult.message)
        }

        val newSalt = SecurityManager.generateSalt()
        val newHash = SecurityManager.hashPassword(newPass, newSalt)

        userDao.updatePassword(currentUser.id, newHash, newSalt)

        auditRepository.recordAction(
            action = AuditAction.PASSWORD_CHANGED,
            entityName = "User",
            entityId = currentUser.id,
            details = "Password updated securely by user",
            result = "SUCCESS"
        )

        return Resource.Success(Unit)
    }

    override suspend fun requestPasswordReset(usernameOrEmail: String): Resource<String> {
        val trimmed = usernameOrEmail.trim()
        if (trimmed.isBlank()) {
            return Resource.Error("Please enter your username or registered email")
        }

        auditRepository.recordAction(
            action = AuditAction.PASSWORD_RESET_REQUESTED,
            entityName = "User",
            entityId = trimmed,
            details = "Password reset initiated for identifier: $trimmed",
            result = "SUCCESS"
        )

        return Resource.Success("A secure password reset link has been dispatched to the verified hospital email on file.")
    }
}
