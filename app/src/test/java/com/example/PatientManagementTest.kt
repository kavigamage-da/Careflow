package com.example

import com.example.core.model.PatientStatus
import com.example.core.model.Permission
import com.example.core.model.RolePermissionMatrix
import com.example.core.model.UserRole
import com.example.core.util.PatientValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class PatientManagementTest {

    @Test
    fun patientValidator_validPatientPasses() {
        val errors = PatientValidator.validate(
            fullName = "Eleanor Vance",
            dob = "1988-04-12",
            phone = "+1-555-0199",
            email = "eleanor.vance@example.com",
            emergencyContactName = "Thomas Vance",
            emergencyContactPhone = "+1-555-0198"
        )
        assertFalse("Valid patient data should have no errors", errors.hasErrors)
    }

    @Test
    fun patientValidator_blankFullNameFails() {
        val errors = PatientValidator.validate(
            fullName = "",
            dob = "1990-01-01",
            phone = "+1-555-0100",
            email = "test@example.com",
            emergencyContactName = "Contact",
            emergencyContactPhone = "+1-555-0101"
        )
        assertTrue(errors.hasErrors)
        assertNotNull(errors.fullName)
    }

    @Test
    fun patientValidator_futureDobFails() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val futureYear = currentYear + 2
        val errors = PatientValidator.validate(
            fullName = "Future Baby",
            dob = "$futureYear-05-15",
            phone = "+1-555-0123",
            email = "future@example.com",
            emergencyContactName = "Parent",
            emergencyContactPhone = "+1-555-0124"
        )
        assertTrue("Future DOB should fail validation", errors.hasErrors)
        assertEquals("Date of birth cannot be in the future", errors.dob)
    }

    @Test
    fun patientValidator_invalidEmailFails() {
        val errors = PatientValidator.validate(
            fullName = "John Doe",
            dob = "1985-06-20",
            phone = "+1-555-0155",
            email = "not-an-email",
            emergencyContactName = "Jane Doe",
            emergencyContactPhone = "+1-555-0156"
        )
        assertTrue(errors.hasErrors)
        assertNotNull(errors.email)
    }

    @Test
    fun patientValidator_invalidPhoneFails() {
        val errors = PatientValidator.validate(
            fullName = "Jane Smith",
            dob = "1992-09-10",
            phone = "123", // too short
            email = "jane@example.com",
            emergencyContactName = "Bob Smith",
            emergencyContactPhone = "+1-555-0188"
        )
        assertTrue(errors.hasErrors)
        assertNotNull(errors.phone)
    }

    @Test
    fun patientValidator_invalidEmergencyContactPhoneFails() {
        val errors = PatientValidator.validate(
            fullName = "Jane Smith",
            dob = "1992-09-10",
            phone = "+1-555-0187",
            email = "jane@example.com",
            emergencyContactName = "Bob Smith",
            emergencyContactPhone = "bad-phone"
        )
        assertTrue(errors.hasErrors)
        assertNotNull(errors.emergencyContactPhone)
    }

    @Test
    fun hospitalRegNo_formatStructureVerification() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val sampleRegNo = "CF-$currentYear-000042"
        val regex = Regex("^CF-\\d{4}-\\d{6}$")
        assertTrue("Hospital Reg No should conform to CF-YYYY-XXXXXX pattern", regex.matches(sampleRegNo))
    }

    @Test
    fun patientStatus_containsRequiredLifecycles() {
        val statuses = PatientStatus.values().map { it.name }
        assertTrue(statuses.contains("ACTIVE"))
        assertTrue(statuses.contains("INACTIVE"))
        assertTrue(statuses.contains("DECEASED"))
        assertTrue(statuses.contains("TRANSFERRED"))
        assertTrue(statuses.contains("ARCHIVED"))
    }

    @Test
    fun patientRBAC_receptionistPermissions() {
        val receptionistPerms = RolePermissionMatrix.getPermissions(UserRole.RECEPTIONIST)

        assertTrue(receptionistPerms.contains(Permission.VIEW_ALL_PATIENTS))
        assertTrue(receptionistPerms.contains(Permission.REGISTER_PATIENT))
        assertTrue(receptionistPerms.contains(Permission.EDIT_PATIENT))
        assertTrue(receptionistPerms.contains(Permission.MANAGE_QUEUE))

        // Receptionist must NOT have administrative archive permission or confidential clinical access
        assertFalse(receptionistPerms.contains(Permission.ARCHIVE_PATIENT))
        assertFalse(receptionistPerms.contains(Permission.VIEW_PATIENT_CLINICAL_TIMELINE))
    }

    @Test
    fun patientRBAC_doctorPermissions() {
        val doctorPerms = RolePermissionMatrix.getPermissions(UserRole.DOCTOR)

        assertTrue(doctorPerms.contains(Permission.VIEW_ALL_PATIENTS))
        assertTrue(doctorPerms.contains(Permission.VIEW_PATIENT_CLINICAL_TIMELINE))

        // Doctors do not have patient registration permission (admitting role)
        assertFalse(doctorPerms.contains(Permission.REGISTER_PATIENT))
    }

    @Test
    fun patientRBAC_patientSelfAccessOnly() {
        val patientPerms = RolePermissionMatrix.getPermissions(UserRole.PATIENT)

        assertTrue(patientPerms.contains(Permission.VIEW_OWN_PROFILE))
        assertFalse(patientPerms.contains(Permission.VIEW_ALL_PATIENTS))
        assertFalse(patientPerms.contains(Permission.REGISTER_PATIENT))
        assertFalse(patientPerms.contains(Permission.EDIT_PATIENT))
        assertFalse(patientPerms.contains(Permission.ARCHIVE_PATIENT))
    }
}
