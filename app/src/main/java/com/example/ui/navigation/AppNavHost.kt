package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.di.LocalAppContainer
import com.example.ui.audit.AuditLogScreen
import com.example.ui.audit.AuditLogViewModel
import com.example.ui.auth.ChangePasswordScreen
import com.example.ui.auth.ForgotPasswordScreen
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.LoginViewModel
import com.example.ui.billing.BillingScreen
import com.example.ui.billing.BillingViewModel
import com.example.ui.consultation.ConsultationScreen
import com.example.ui.consultation.ConsultationViewModel
import com.example.ui.dashboard.RoleDashboardScreen
import com.example.ui.dashboard.RoleDashboardViewModel
import com.example.ui.lab.LabScreen
import com.example.ui.lab.LabViewModel
import com.example.ui.patient.PatientEditScreen
import com.example.ui.patient.PatientEditViewModel
import com.example.ui.patient.PatientProfileScreen
import com.example.ui.patient.PatientProfileViewModel
import com.example.ui.patient.PatientRegisterScreen
import com.example.ui.patient.PatientRegisterViewModel
import com.example.ui.patient.PatientSearchScreen
import com.example.ui.patient.PatientSearchViewModel
import com.example.ui.pharmacy.PharmacyScreen
import com.example.ui.pharmacy.PharmacyViewModel
import com.example.ui.queue.QueueScreen
import com.example.ui.queue.QueueViewModel
import com.example.ui.reports.ReportsScreen
import com.example.ui.reports.ReportsViewModel
import com.example.ui.settings.HospitalSettingsScreen
import com.example.ui.settings.HospitalSettingsViewModel
import com.example.ui.triage.TriageScreen
import com.example.ui.triage.TriageViewModel

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    val container = LocalAppContainer.current

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModel.provideFactory(container.authRepository)
            )
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onNavigateChangePassword = {
                    navController.navigate(Screen.ChangePassword.route)
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            BackHandler {
                navController.popBackStack()
            }
            ForgotPasswordScreen(
                authRepository = container.authRepository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ChangePassword.route) {
            BackHandler {
                navController.popBackStack()
            }
            ChangePasswordScreen(
                authRepository = container.authRepository,
                onPasswordChanged = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.ChangePassword.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Dashboard.route) {
            val dashboardViewModel: RoleDashboardViewModel = viewModel(
                factory = RoleDashboardViewModel.provideFactory(
                    container.authRepository,
                    container.database
                )
            )
            RoleDashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateAuditLogs = {
                    navController.navigate(Screen.AuditLogs.route)
                },
                onNavigateSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigatePatientSearch = {
                    navController.navigate(Screen.PatientSearch.route)
                },
                onNavigatePatientRegister = {
                    navController.navigate(Screen.PatientRegister.route)
                },
                onNavigatePatientProfile = { patientId ->
                    navController.navigate(Screen.PatientProfile.createRoute(patientId))
                },
                onNavigateQueue = {
                    navController.navigate(Screen.Queue.route)
                },
                onNavigatePharmacy = {
                    navController.navigate(Screen.Pharmacy.route)
                },
                onNavigateLaboratory = {
                    navController.navigate(Screen.Laboratory.route)
                },
                onNavigateBilling = {
                    navController.navigate(Screen.Billing.route)
                },
                onNavigateReports = {
                    navController.navigate(Screen.Reports.route)
                },
                onLoggedOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AuditLogs.route) {
            BackHandler {
                navController.popBackStack()
            }
            val auditViewModel: AuditLogViewModel = viewModel(
                factory = AuditLogViewModel.provideFactory(container.auditRepository)
            )
            AuditLogScreen(
                viewModel = auditViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            BackHandler {
                navController.popBackStack()
            }
            val settingsViewModel: HospitalSettingsViewModel = viewModel(
                factory = HospitalSettingsViewModel.provideFactory(container.settingsRepository)
            )
            HospitalSettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PatientSearch.route) {
            BackHandler {
                navController.popBackStack()
            }
            val searchViewModel: PatientSearchViewModel = viewModel(
                factory = PatientSearchViewModel.provideFactory(
                    container.patientRepository,
                    container.authRepository
                )
            )
            PatientSearchScreen(
                viewModel = searchViewModel,
                onNavigateRegister = {
                    navController.navigate(Screen.PatientRegister.route)
                },
                onNavigateProfile = { patientId ->
                    navController.navigate(Screen.PatientProfile.createRoute(patientId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PatientRegister.route) {
            BackHandler {
                navController.popBackStack()
            }
            val registerViewModel: PatientRegisterViewModel = viewModel(
                factory = PatientRegisterViewModel.provideFactory(
                    container.patientRepository,
                    container.authRepository
                )
            )
            PatientRegisterScreen(
                viewModel = registerViewModel,
                onNavigateProfile = { patientId ->
                    navController.navigate(Screen.PatientProfile.createRoute(patientId)) {
                        popUpTo(Screen.PatientRegister.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PatientProfile.route,
            arguments = listOf(navArgument("patientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId") ?: ""
            BackHandler {
                navController.popBackStack()
            }
            val profileViewModel: PatientProfileViewModel = viewModel(
                factory = PatientProfileViewModel.provideFactory(
                    patientId,
                    container.patientRepository,
                    container.authRepository,
                    container.database
                )
            )
            PatientProfileScreen(
                viewModel = profileViewModel,
                onNavigateEdit = { pId ->
                    navController.navigate(Screen.PatientEdit.createRoute(pId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PatientEdit.route,
            arguments = listOf(navArgument("patientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId") ?: ""
            BackHandler {
                navController.popBackStack()
            }
            val editViewModel: PatientEditViewModel = viewModel(
                factory = PatientEditViewModel.provideFactory(
                    patientId,
                    container.patientRepository,
                    container.authRepository
                )
            )
            PatientEditScreen(
                viewModel = editViewModel,
                onSaveSuccess = {
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Queue.route) {
            BackHandler { navController.popBackStack() }
            val queueViewModel: QueueViewModel = viewModel(
                factory = QueueViewModel.provideFactory(
                    container.queueRepository,
                    container.patientRepository,
                    container.sessionManager
                )
            )
            QueueScreen(
                viewModel = queueViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateAuditLogs = { navController.navigate(Screen.AuditLogs.route) },
                onNavigateSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateTriage = { ticketId ->
                    navController.navigate(Screen.Triage.createRoute(ticketId))
                },
                onNavigateConsultation = { ticketId ->
                    navController.navigate(Screen.Consultation.createRoute(ticketId))
                },
                onNavigatePatientProfile = { patientId ->
                    navController.navigate(Screen.PatientProfile.createRoute(patientId))
                }
            )
        }

        composable(
            route = Screen.Triage.route,
            arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
            BackHandler { navController.popBackStack() }
            val triageViewModel: TriageViewModel = viewModel(
                factory = TriageViewModel.provideFactory(
                    ticketId,
                    container.queueRepository,
                    container.patientRepository,
                    container.clinicalRepository
                )
            )
            TriageScreen(
                viewModel = triageViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateAuditLogs = { navController.navigate(Screen.AuditLogs.route) },
                onNavigateSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.Consultation.route,
            arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
            BackHandler { navController.popBackStack() }
            val consultViewModel: ConsultationViewModel = viewModel(
                factory = ConsultationViewModel.provideFactory(
                    ticketId,
                    container.queueRepository,
                    container.patientRepository,
                    container.clinicalRepository,
                    container.billingRepository
                )
            )
            ConsultationScreen(
                viewModel = consultViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateAuditLogs = { navController.navigate(Screen.AuditLogs.route) },
                onNavigateSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Pharmacy.route) {
            BackHandler { navController.popBackStack() }
            val pharmacyViewModel: PharmacyViewModel = viewModel(
                factory = PharmacyViewModel.provideFactory(
                    container.pharmacyRepository,
                    container.patientRepository
                )
            )
            PharmacyScreen(
                viewModel = pharmacyViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateAuditLogs = { navController.navigate(Screen.AuditLogs.route) },
                onNavigateSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Laboratory.route) {
            BackHandler { navController.popBackStack() }
            val labViewModel: LabViewModel = viewModel(
                factory = LabViewModel.provideFactory(container.labRepository)
            )
            LabScreen(
                viewModel = labViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateAuditLogs = { navController.navigate(Screen.AuditLogs.route) },
                onNavigateSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Billing.route) {
            BackHandler { navController.popBackStack() }
            val billingViewModel: BillingViewModel = viewModel(
                factory = BillingViewModel.provideFactory(container.billingRepository)
            )
            BillingScreen(
                viewModel = billingViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateAuditLogs = { navController.navigate(Screen.AuditLogs.route) },
                onNavigateSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Reports.route) {
            BackHandler { navController.popBackStack() }
            val reportsViewModel: ReportsViewModel = viewModel(
                factory = ReportsViewModel.provideFactory(container.database)
            )
            ReportsScreen(
                viewModel = reportsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateAuditLogs = { navController.navigate(Screen.AuditLogs.route) },
                onNavigateSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
    }
}
