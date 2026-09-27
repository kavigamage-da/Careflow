package com.example.core.model

enum class TimelineEventType(val label: String) {
    REGISTRATION("Patient Registration"),
    APPOINTMENT("Appointment Scheduled"),
    CHECK_IN("Checked In to Queue"),
    TRIAGE_VITALS("Triage Vital Signs"),
    CLINICAL_CONSULTATION("Doctor Consultation"),
    PRESCRIPTION("Prescription Issued"),
    LAB_ORDER("Laboratory Test Ordered"),
    LAB_RESULT("Lab Result Verified"),
    INVOICE("Invoice Generated"),
    PAYMENT("Payment Received")
}

data class PatientTimelineEvent(
    val id: String,
    val timestamp: Long,
    val type: TimelineEventType,
    val title: String,
    val department: String,
    val practitionerName: String,
    val summary: String,
    val status: String,
    val isConfidentialClinical: Boolean = false
)
