package com.example.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PatientFormErrors(
    val fullName: String? = null,
    val dob: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null
) {
    val hasErrors: Boolean
        get() = fullName != null || dob != null || phone != null ||
                email != null || emergencyContactName != null || emergencyContactPhone != null
}

object PatientValidator {

    private val dobFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        isLenient = false
    }

    fun validate(
        fullName: String,
        dob: String,
        phone: String,
        email: String,
        emergencyContactName: String,
        emergencyContactPhone: String
    ): PatientFormErrors {
        val trimmedName = fullName.trim()
        val nameError = when {
            trimmedName.isBlank() -> "Full name is required"
            trimmedName.length < 3 -> "Full name must be at least 3 characters"
            trimmedName.any { it.isDigit() } -> "Full name should not contain numbers"
            else -> null
        }

        val dobError = validateDob(dob)

        val trimmedPhone = phone.trim()
        val phoneError = when {
            trimmedPhone.isBlank() -> "Phone number is required"
            trimmedPhone.replace(Regex("[^0-9+]"), "").length < 7 -> "Please enter a valid phone number (at least 7 digits)"
            else -> null
        }

        val trimmedEmail = email.trim()
        val emailError = if (trimmedEmail.isNotBlank()) {
            val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
            if (!emailRegex.matches(trimmedEmail)) "Please enter a valid email address" else null
        } else null

        val trimmedEmergencyName = emergencyContactName.trim()
        val emergencyNameError = when {
            trimmedEmergencyName.isBlank() -> "Emergency contact name is required"
            trimmedEmergencyName.length < 2 -> "Name must be at least 2 characters"
            else -> null
        }

        val trimmedEmergencyPhone = emergencyContactPhone.trim()
        val emergencyPhoneError = when {
            trimmedEmergencyPhone.isBlank() -> "Emergency contact phone is required"
            trimmedEmergencyPhone.replace(Regex("[^0-9+]"), "").length < 7 -> "Please enter a valid emergency phone number"
            else -> null
        }

        return PatientFormErrors(
            fullName = nameError,
            dob = dobError,
            phone = phoneError,
            email = emailError,
            emergencyContactName = emergencyNameError,
            emergencyContactPhone = emergencyPhoneError
        )
    }

    fun validateDob(dobString: String): String? {
        val trimmed = dobString.trim()
        if (trimmed.isBlank()) {
            return "Date of birth is required (YYYY-MM-DD)"
        }
        val dateRegex = "^\\d{4}-\\d{2}-\\d{2}\$".toRegex()
        if (!dateRegex.matches(trimmed)) {
            return "Format must be YYYY-MM-DD (e.g. 1990-05-24)"
        }
        return try {
            val parsedDate = dobFormat.parse(trimmed)
            if (parsedDate == null) {
                "Invalid calendar date"
            } else if (parsedDate.after(Date())) {
                "Date of birth cannot be in the future"
            } else {
                // Check reasonable past limit (e.g. 130 years)
                val minYearMillis = System.currentTimeMillis() - (130L * 365.25 * 24 * 60 * 60 * 1000).toLong()
                if (parsedDate.time < minYearMillis) {
                    "Date of birth exceeds maximum reasonable age"
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            "Invalid date format (must be YYYY-MM-DD)"
        }
    }
}
