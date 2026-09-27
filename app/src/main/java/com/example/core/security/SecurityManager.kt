package com.example.core.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

object SecurityManager {

    private val secureRandom = SecureRandom()

    fun generateSalt(): String {
        val saltBytes = ByteArray(16)
        secureRandom.nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val combined = "$salt:$password"
        val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(candidatePassword: String, salt: String, expectedHash: String): Boolean {
        val candidateHash = hashPassword(candidatePassword, salt)
        return MessageDigest.isEqual(
            candidateHash.toByteArray(Charsets.UTF_8),
            expectedHash.toByteArray(Charsets.UTF_8)
        )
    }

    fun generateSessionToken(): String {
        return UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis()
    }

    fun validatePasswordPolicy(password: String): PasswordValidationResult {
        if (password.length < 8) {
            return PasswordValidationResult(false, "Password must be at least 8 characters long")
        }
        if (!password.any { it.isUpperCase() }) {
            return PasswordValidationResult(false, "Password must contain at least one uppercase letter")
        }
        if (!password.any { it.isLowerCase() }) {
            return PasswordValidationResult(false, "Password must contain at least one lowercase letter")
        }
        if (!password.any { it.isDigit() }) {
            return PasswordValidationResult(false, "Password must contain at least one digit")
        }
        val specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?"
        if (!password.any { it in specialChars }) {
            return PasswordValidationResult(false, "Password must contain at least one special character")
        }
        return PasswordValidationResult(true, "Password meets security policy requirements")
    }
}

data class PasswordValidationResult(
    val isValid: Boolean,
    val message: String
)
