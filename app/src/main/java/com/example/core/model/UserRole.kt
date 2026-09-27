package com.example.core.model

enum class UserRole(val displayName: String, val description: String) {
    PATIENT("Patient", "Access to personal medical records, queue status, and appointment requests"),
    RECEPTIONIST("Receptionist", "Patient search, walk-in registration, check-in, and scheduling"),
    QUEUE_OPERATOR("Queue Operator", "Queue dispatch, ticket management, call next, and room routing"),
    DOCTOR("Doctor", "Clinical consultation, diagnosis, prescription issuing, and lab ordering"),
    NURSE("Nurse", "Triage, vital signs recording, nursing tasks, and patient preparation"),
    LAB_TECHNICIAN("Lab Technician", "Sample collection, test processing, and diagnostic result entry"),
    PHARMACIST("Pharmacist", "Prescription verification, medicine dispensing, and stock tracking"),
    CASHIER("Cashier", "Billing, invoice generation, payment processing, and receipts"),
    HOSPITAL_ADMIN("Hospital Admin", "Operational dashboard, clinical audits, department and staff governance"),
    SUPER_ADMIN("Super Admin", "Complete system administration, access configuration, and security management")
}
