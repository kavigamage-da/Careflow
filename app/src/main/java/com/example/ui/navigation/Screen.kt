package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object ForgotPassword : Screen("forgot_password")
    object ChangePassword : Screen("change_password")
    object Dashboard : Screen("dashboard")
    object AuditLogs : Screen("audit_logs")
    object Settings : Screen("settings")
    object PatientSearch : Screen("patient_search")
    object PatientRegister : Screen("patient_register")
    object PatientProfile : Screen("patient_profile/{patientId}") {
        fun createRoute(patientId: String) = "patient_profile/$patientId"
    }
    object PatientEdit : Screen("patient_edit/{patientId}") {
        fun createRoute(patientId: String) = "patient_edit/$patientId"
    }
    object Queue : Screen("queue")
    object Triage : Screen("triage/{ticketId}") {
        fun createRoute(ticketId: String) = "triage/$ticketId"
    }
    object Consultation : Screen("consultation/{ticketId}") {
        fun createRoute(ticketId: String) = "consultation/$ticketId"
    }
    object Pharmacy : Screen("pharmacy")
    object Laboratory : Screen("laboratory")
    object Billing : Screen("billing")
    object Reports : Screen("reports")
}
