package com.example.core.model

enum class Permission(val description: String) {
    // Patient Management
    VIEW_OWN_PROFILE("View own patient profile and history"),
    VIEW_ALL_PATIENTS("Search and view any patient records"),
    REGISTER_PATIENT("Register a new patient"),
    EDIT_PATIENT("Update patient demographic info"),
    SEARCH_PATIENT("Search patient directory"),
    ARCHIVE_PATIENT("Archive or deactivate patient record"),
    VIEW_PATIENT_CLINICAL_TIMELINE("View confidential clinical encounters and diagnoses"),

    // Appointments & Queue
    BOOK_APPOINTMENT("Book or request an appointment"),
    CANCEL_APPOINTMENT("Cancel an appointment"),
    MANAGE_QUEUE("Generate queue tickets and manage queue status"),
    CALL_NEXT_PATIENT("Call next patient in line to consultation room"),
    RECALL_PATIENT("Recall or re-page a called patient"),
    SKIP_QUEUE_PATIENT("Mark unresponsive patient as skipped"),
    CANCEL_QUEUE_TICKET("Cancel active queue ticket"),
    TRANSFER_QUEUE_PATIENT("Transfer queue ticket between hospital departments"),
    ASSIGN_QUEUE_DOCTOR("Assign or re-route ticket to a specific physician"),
    START_CONSULTATION("Start clinical consultation session"),
    COMPLETE_QUEUE("Conclude consultation and complete queue ticket"),
    VIEW_QUEUE_HISTORY("View and search historical queue records"),
    ASSIGN_EMERGENCY_PRIORITY("Authorize emergency triage queue priority override"),
    VIEW_QUEUE_DISPLAY("Access waiting area live terminal display board"),

    // Clinical & Nursing
    RECORD_VITALS("Record patient vital signs and triage status"),
    PERFORM_CONSULTATION("Perform doctor consultation and clinical diagnosis"),
    VIEW_CLINICAL_HISTORY("View previous clinical notes and encounters"),

    // Prescriptions & Pharmacy
    ORDER_PRESCRIPTION("Create and issue medicine prescriptions"),
    DISPENSE_PRESCRIPTION("Verify and dispense medication"),
    MANAGE_INVENTORY("Update medicine stock and inventory adjustments"),

    // Laboratory
    ORDER_LAB_TEST("Order diagnostic laboratory tests"),
    ENTER_LAB_RESULT("Enter diagnostic laboratory test results"),
    VERIFY_LAB_RESULT("Verify and authorize laboratory results"),

    // Billing & Finance
    CREATE_INVOICE("Create invoices for hospital encounters"),
    RECORD_PAYMENT("Record patient payments and print receipts"),
    VIEW_FINANCIAL_REPORTS("View revenue and financial metrics"),

    // Governance & Security
    VIEW_AUDIT_LOGS("Inspect immutable security and clinical audit trails"),
    MANAGE_HOSPITAL_SETTINGS("Configure hospital profile, working hours, and policies"),
    MANAGE_USERS("Create, update, lock, and unlock system users"),
    MANAGE_DEPARTMENTS("Manage hospital departments and doctor assignments")
}
