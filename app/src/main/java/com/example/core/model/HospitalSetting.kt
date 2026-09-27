package com.example.core.model

data class HospitalSetting(
    val key: String,
    val value: String,
    val description: String,
    val updatedAt: Long
)

object HospitalSettingKeys {
    const val HOSPITAL_NAME = "hospital_name"
    const val EMERGENCY_CONTACT = "emergency_contact"
    const val HOSPITAL_ADDRESS = "hospital_address"
    const val HOSPITAL_EMAIL = "hospital_email"
    const val APPOINTMENT_SLOT_MINUTES = "appointment_slot_minutes"
    const val SESSION_TIMEOUT_MINUTES = "session_timeout_minutes"
    const val MAX_LOGIN_ATTEMPTS = "max_login_attempts"
    const val CURRENCY_SYMBOL = "currency_symbol"
    const val IS_DEMO_ENVIRONMENT = "is_demo_environment"
}
