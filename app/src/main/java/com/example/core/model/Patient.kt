package com.example.core.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class Patient(
    val id: String,
    val hospitalRegNo: String,
    val fullName: String,
    val dob: String, // YYYY-MM-DD
    val gender: String,
    val nationalIdOrPassport: String,
    val phone: String,
    val email: String,
    val address: String,
    val preferredLanguage: String,
    val bloodGroup: String,
    val allergies: String,
    val existingConditions: String,
    val emergencyContactName: String,
    val emergencyContactRelationship: String,
    val emergencyContactPhone: String,
    val emergencyContactSecondaryPhone: String,
    val registeredDate: Long,
    val status: PatientStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val createdBy: String,
    val updatedBy: String
) {
    val age: Int
        get() {
            return try {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val birthDate = format.parse(dob) ?: return 0
                val birthCalendar = Calendar.getInstance().apply { time = birthDate }
                val todayCalendar = Calendar.getInstance()
                var diff = todayCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
                if (todayCalendar.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                    diff--
                }
                if (diff < 0) 0 else diff
            } catch (e: Exception) {
                0
            }
        }
}
