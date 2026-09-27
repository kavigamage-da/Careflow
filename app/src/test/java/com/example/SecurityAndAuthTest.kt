package com.example

import com.example.core.model.AuthUser
import com.example.core.model.Permission
import com.example.core.model.RolePermissionMatrix
import com.example.core.model.UserRole
import com.example.core.security.SecurityManager
import com.example.core.security.SessionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityAndAuthTest {

    @Test
    fun passwordHashing_generatesSecureAndVerifiableHash() {
        val password = "HospitalAdmin123!"
        val salt = SecurityManager.generateSalt()
        val hash = SecurityManager.hashPassword(password, salt)

        assertNotNull(salt)
        assertTrue(salt.isNotEmpty())
        assertTrue(hash.isNotEmpty())

        // Verify valid candidate
        val isValid = SecurityManager.verifyPassword(password, salt, hash)
        assertTrue("Password verification must succeed with correct password", isValid)

        // Verify invalid candidate
        val isInvalid = SecurityManager.verifyPassword("WrongPassword123!", salt, hash)
        assertFalse("Password verification must fail with wrong password", isInvalid)
    }

    @Test
    fun passwordPolicy_enforcesEnterpriseRules() {
        // Too short
        val shortResult = SecurityManager.validatePasswordPolicy("Ab1!")
        assertFalse(shortResult.isValid)

        // No uppercase
        val noUpper = SecurityManager.validatePasswordPolicy("password123!")
        assertFalse(noUpper.isValid)

        // No number
        val noDigit = SecurityManager.validatePasswordPolicy("Password!")
        assertFalse(noDigit.isValid)

        // No special char
        val noSpecial = SecurityManager.validatePasswordPolicy("Password123")
        assertFalse(noSpecial.isValid)

        // Valid
        val validResult = SecurityManager.validatePasswordPolicy("SecureCare2026!")
        assertTrue(validResult.isValid)
    }

    @Test
    fun rolePermissionMatrix_enforcesStrictSeparationOfDuties() {
        // Patient can view own profile and book appointment, but cannot consult or dispense
        assertTrue(RolePermissionMatrix.hasPermission(UserRole.PATIENT, Permission.VIEW_OWN_PROFILE))
        assertTrue(RolePermissionMatrix.hasPermission(UserRole.PATIENT, Permission.BOOK_APPOINTMENT))
        assertFalse(RolePermissionMatrix.hasPermission(UserRole.PATIENT, Permission.PERFORM_CONSULTATION))
        assertFalse(RolePermissionMatrix.hasPermission(UserRole.PATIENT, Permission.DISPENSE_PRESCRIPTION))

        // Doctor can consult, order prescription, but cannot manage hospital settings or staff
        assertTrue(RolePermissionMatrix.hasPermission(UserRole.DOCTOR, Permission.PERFORM_CONSULTATION))
        assertTrue(RolePermissionMatrix.hasPermission(UserRole.DOCTOR, Permission.ORDER_PRESCRIPTION))
        assertFalse(RolePermissionMatrix.hasPermission(UserRole.DOCTOR, Permission.MANAGE_HOSPITAL_SETTINGS))

        // Nurse can record vitals and triage
        assertTrue(RolePermissionMatrix.hasPermission(UserRole.NURSE, Permission.RECORD_VITALS))
        assertFalse(RolePermissionMatrix.hasPermission(UserRole.NURSE, Permission.PERFORM_CONSULTATION))

        // Pharmacist can dispense prescription
        assertTrue(RolePermissionMatrix.hasPermission(UserRole.PHARMACIST, Permission.DISPENSE_PRESCRIPTION))
        assertFalse(RolePermissionMatrix.hasPermission(UserRole.PHARMACIST, Permission.ORDER_PRESCRIPTION))

        // Cashier can create invoice and record payments
        assertTrue(RolePermissionMatrix.hasPermission(UserRole.CASHIER, Permission.RECORD_PAYMENT))
        assertFalse(RolePermissionMatrix.hasPermission(UserRole.CASHIER, Permission.PERFORM_CONSULTATION))

        // Hospital Admin can view audit logs and manage settings
        assertTrue(RolePermissionMatrix.hasPermission(UserRole.HOSPITAL_ADMIN, Permission.VIEW_AUDIT_LOGS))
        assertTrue(RolePermissionMatrix.hasPermission(UserRole.HOSPITAL_ADMIN, Permission.MANAGE_HOSPITAL_SETTINGS))

        // Super Admin has all permissions
        Permission.values().forEach { permission ->
            assertTrue("Super Admin must have permission $permission", RolePermissionMatrix.hasPermission(UserRole.SUPER_ADMIN, permission))
        }
    }

    @Test
    fun sessionManager_tracksInactivityAndTermination() {
        val sessionManager = SessionManager()
        sessionManager.configureTimeout(30)

        val user = AuthUser(
            id = "u_test",
            username = "test.doctor",
            fullName = "Dr. Test",
            role = UserRole.DOCTOR,
            departmentId = "dept_cardio",
            email = "test@hospital.health",
            phone = "+15550001",
            lastLoginAt = System.currentTimeMillis()
        )

        sessionManager.startSession(user)
        assertEquals(user, sessionManager.currentUser.value)
        assertFalse("New session must not be expired", sessionManager.isSessionExpired())

        sessionManager.terminateSession()
        assertEquals(null, sessionManager.currentUser.value)
    }
}
