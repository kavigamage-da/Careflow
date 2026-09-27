package com.example.core.model

enum class PatientStatus(val displayName: String) {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    DECEASED("Deceased"),
    TRANSFERRED("Transferred"),
    ARCHIVED("Archived")
}
