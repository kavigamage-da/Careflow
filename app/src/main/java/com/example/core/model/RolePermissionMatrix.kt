package com.example.core.model

object RolePermissionMatrix {

    private val rolePermissions: Map<UserRole, Set<Permission>> = mapOf(
        UserRole.PATIENT to setOf(
            Permission.VIEW_OWN_PROFILE,
            Permission.BOOK_APPOINTMENT,
            Permission.CANCEL_APPOINTMENT,
            Permission.VIEW_QUEUE_DISPLAY,
            Permission.VIEW_QUEUE_HISTORY
        ),

        UserRole.RECEPTIONIST to setOf(
            Permission.VIEW_OWN_PROFILE,
            Permission.VIEW_ALL_PATIENTS,
            Permission.SEARCH_PATIENT,
            Permission.REGISTER_PATIENT,
            Permission.EDIT_PATIENT,
            Permission.BOOK_APPOINTMENT,
            Permission.CANCEL_APPOINTMENT,
            Permission.MANAGE_QUEUE,
            Permission.TRANSFER_QUEUE_PATIENT,
            Permission.ASSIGN_QUEUE_DOCTOR,
            Permission.CANCEL_QUEUE_TICKET,
            Permission.VIEW_QUEUE_DISPLAY,
            Permission.VIEW_QUEUE_HISTORY
        ),

        UserRole.QUEUE_OPERATOR to setOf(
            Permission.VIEW_OWN_PROFILE,
            Permission.MANAGE_QUEUE,
            Permission.CALL_NEXT_PATIENT,
            Permission.RECALL_PATIENT,
            Permission.SKIP_QUEUE_PATIENT,
            Permission.CANCEL_QUEUE_TICKET,
            Permission.TRANSFER_QUEUE_PATIENT,
            Permission.ASSIGN_QUEUE_DOCTOR,
            Permission.VIEW_QUEUE_DISPLAY,
            Permission.VIEW_QUEUE_HISTORY
        ),

        UserRole.DOCTOR to setOf(
            Permission.VIEW_OWN_PROFILE,
            Permission.VIEW_ALL_PATIENTS,
            Permission.SEARCH_PATIENT,
            Permission.VIEW_PATIENT_CLINICAL_TIMELINE,
            Permission.CALL_NEXT_PATIENT,
            Permission.RECALL_PATIENT,
            Permission.SKIP_QUEUE_PATIENT,
            Permission.START_CONSULTATION,
            Permission.COMPLETE_QUEUE,
            Permission.TRANSFER_QUEUE_PATIENT,
            Permission.ASSIGN_EMERGENCY_PRIORITY,
            Permission.VIEW_QUEUE_DISPLAY,
            Permission.VIEW_QUEUE_HISTORY,
            Permission.VIEW_CLINICAL_HISTORY,
            Permission.PERFORM_CONSULTATION,
            Permission.RECORD_VITALS,
            Permission.ORDER_PRESCRIPTION,
            Permission.ORDER_LAB_TEST,
            Permission.VERIFY_LAB_RESULT
        ),

        UserRole.NURSE to setOf(
            Permission.VIEW_OWN_PROFILE,
            Permission.VIEW_ALL_PATIENTS,
            Permission.SEARCH_PATIENT,
            Permission.VIEW_PATIENT_CLINICAL_TIMELINE,
            Permission.RECORD_VITALS,
            Permission.VIEW_CLINICAL_HISTORY,
            Permission.MANAGE_QUEUE,
            Permission.CALL_NEXT_PATIENT,
            Permission.RECALL_PATIENT,
            Permission.SKIP_QUEUE_PATIENT,
            Permission.ASSIGN_EMERGENCY_PRIORITY,
            Permission.VIEW_QUEUE_DISPLAY,
            Permission.VIEW_QUEUE_HISTORY
        ),

        UserRole.LAB_TECHNICIAN to setOf(
            Permission.VIEW_OWN_PROFILE,
            Permission.ENTER_LAB_RESULT,
            Permission.VERIFY_LAB_RESULT
        ),

        UserRole.PHARMACIST to setOf(
            Permission.VIEW_OWN_PROFILE,
            Permission.DISPENSE_PRESCRIPTION,
            Permission.MANAGE_INVENTORY
        ),

        UserRole.CASHIER to setOf(
            Permission.VIEW_OWN_PROFILE,
            Permission.VIEW_ALL_PATIENTS,
            Permission.SEARCH_PATIENT,
            Permission.CREATE_INVOICE,
            Permission.RECORD_PAYMENT
        ),

        UserRole.HOSPITAL_ADMIN to setOf(
            Permission.VIEW_OWN_PROFILE,
            Permission.VIEW_ALL_PATIENTS,
            Permission.SEARCH_PATIENT,
            Permission.REGISTER_PATIENT,
            Permission.EDIT_PATIENT,
            Permission.ARCHIVE_PATIENT,
            Permission.VIEW_PATIENT_CLINICAL_TIMELINE,
            Permission.BOOK_APPOINTMENT,
            Permission.CANCEL_APPOINTMENT,
            Permission.MANAGE_QUEUE,
            Permission.CALL_NEXT_PATIENT,
            Permission.RECALL_PATIENT,
            Permission.SKIP_QUEUE_PATIENT,
            Permission.CANCEL_QUEUE_TICKET,
            Permission.TRANSFER_QUEUE_PATIENT,
            Permission.ASSIGN_QUEUE_DOCTOR,
            Permission.START_CONSULTATION,
            Permission.COMPLETE_QUEUE,
            Permission.VIEW_QUEUE_HISTORY,
            Permission.ASSIGN_EMERGENCY_PRIORITY,
            Permission.VIEW_QUEUE_DISPLAY,
            Permission.VIEW_FINANCIAL_REPORTS,
            Permission.VIEW_AUDIT_LOGS,
            Permission.MANAGE_HOSPITAL_SETTINGS,
            Permission.MANAGE_DEPARTMENTS
        ),

        UserRole.SUPER_ADMIN to Permission.values().toSet()
    )

    fun hasPermission(role: UserRole, permission: Permission): Boolean {
        return rolePermissions[role]?.contains(permission) == true
    }

    fun getPermissions(role: UserRole): Set<Permission> {
        return rolePermissions[role] ?: emptySet()
    }
}
