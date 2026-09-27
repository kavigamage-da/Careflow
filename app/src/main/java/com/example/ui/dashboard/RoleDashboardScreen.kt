package com.example.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.AuthUser
import com.example.core.model.Permission
import com.example.core.model.UserRole
import com.example.core.util.DateTimeUtils
import com.example.ui.components.CareFlowScaffold
import com.example.ui.components.MetricCard
import com.example.ui.components.StatusChip

@Composable
fun RoleDashboardScreen(
    viewModel: RoleDashboardViewModel,
    onNavigateAuditLogs: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigatePatientSearch: () -> Unit = {},
    onNavigatePatientRegister: () -> Unit = {},
    onNavigatePatientProfile: (String) -> Unit = {},
    onNavigateQueue: () -> Unit = {},
    onNavigatePharmacy: () -> Unit = {},
    onNavigateLaboratory: () -> Unit = {},
    onNavigateBilling: () -> Unit = {},
    onNavigateReports: () -> Unit = {},
    onLoggedOut: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val metrics by viewModel.metrics.collectAsStateWithLifecycle()

    CareFlowScaffold(
        title = "CareFlow Terminal",
        currentUser = currentUser,
        canNavigateBack = false,
        onNavigateAuditLogs = onNavigateAuditLogs,
        onNavigateSettings = onNavigateSettings,
        onLogout = { viewModel.logout(onLoggedOut) }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Staff Profile Header Card
            item {
                currentUser?.let { user ->
                    StaffProfileHeader(user = user)
                }
            }

            // Role-Specific Metrics
            item {
                currentUser?.let { user ->
                    when (user.role) {
                        UserRole.HOSPITAL_ADMIN, UserRole.SUPER_ADMIN -> {
                            AdminMetricsSection(
                                metrics = metrics,
                                onNavigateAuditLogs = onNavigateAuditLogs,
                                onNavigateSettings = onNavigateSettings,
                                onNavigatePatientSearch = onNavigatePatientSearch,
                                onNavigateReports = onNavigateReports,
                                onNavigateQueue = onNavigateQueue
                            )
                        }
                        UserRole.DOCTOR -> {
                            DoctorMetricsSection(
                                metrics = metrics,
                                onNavigatePatientSearch = onNavigatePatientSearch,
                                onNavigateQueue = onNavigateQueue
                            )
                        }
                        UserRole.RECEPTIONIST -> {
                            ReceptionMetricsSection(
                                metrics = metrics,
                                onNavigatePatientSearch = onNavigatePatientSearch,
                                onNavigatePatientRegister = onNavigatePatientRegister,
                                onNavigateQueue = onNavigateQueue
                            )
                        }
                        UserRole.NURSE -> {
                            NurseMetricsSection(
                                metrics = metrics,
                                onNavigateQueue = onNavigateQueue
                            )
                        }
                        UserRole.QUEUE_OPERATOR -> {
                            QueueOperatorMetricsSection(
                                metrics = metrics,
                                onNavigateQueue = onNavigateQueue
                            )
                        }
                        UserRole.LAB_TECHNICIAN -> {
                            LabMetricsSection(
                                metrics = metrics,
                                onNavigateLaboratory = onNavigateLaboratory
                            )
                        }
                        UserRole.PHARMACIST -> {
                            PharmacyMetricsSection(
                                metrics = metrics,
                                onNavigatePharmacy = onNavigatePharmacy
                            )
                        }
                        UserRole.CASHIER -> {
                            CashierMetricsSection(
                                metrics = metrics,
                                onNavigateBilling = onNavigateBilling
                            )
                        }
                        UserRole.PATIENT -> {
                            PatientPortalSection(
                                metrics = metrics,
                                onNavigateProfile = { onNavigatePatientProfile("pat_001") }
                            )
                        }
                    }
                }
            }

            // Live Queue Preview Section
            item {
                LiveQueuePreviewCard(metrics = metrics)
            }

            // Medical Safety Disclaimer
            item {
                MedicalSafetyDisclaimerCard()
            }
        }
    }
}

@Composable
private fun StaffProfileHeader(user: AuthUser) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = user.fullName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = user.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${user.role.displayName} • ${user.departmentId ?: "General Facility"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                if (user.lastLoginAt != null) {
                    Text(
                        text = "Session active • Last login: ${DateTimeUtils.formatTime(user.lastLoginAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminMetricsSection(
    metrics: DashboardMetrics,
    onNavigateAuditLogs: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigatePatientSearch: () -> Unit,
    onNavigateReports: () -> Unit,
    onNavigateQueue: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Hospital Operations KPI Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MetricCard(
                title = "Waiting Queue",
                value = "${metrics.waitingQueueCount}",
                icon = Icons.Default.ConfirmationNumber,
                iconColor = MaterialTheme.colorScheme.primary,
                subtitle = "Active in OPD",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Today's Appts",
                value = "${metrics.todayAppointments}",
                icon = Icons.Default.CalendarMonth,
                iconColor = Color(0xFF0288D1),
                subtitle = "Scheduled",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MetricCard(
                title = "Pending Rx & Labs",
                value = "${metrics.pendingPrescriptions + metrics.pendingLabOrders}",
                icon = Icons.Default.Biotech,
                iconColor = Color(0xFFE65100),
                subtitle = "${metrics.pendingPrescriptions} Rx / ${metrics.pendingLabOrders} Labs",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Revenue (Today)",
                value = "$${metrics.totalRevenue.toInt()}",
                icon = Icons.Default.AttachMoney,
                iconColor = Color(0xFF2E7D32),
                subtitle = "${metrics.pendingInvoices} unpaid bills",
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Administrative Governance & Operations",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onNavigateReports,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reports", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onNavigateQueue,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Queue", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onNavigatePatientSearch,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Patients", fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onNavigateAuditLogs,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Audit Trail", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onNavigateSettings,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Settings", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DoctorMetricsSection(
    metrics: DashboardMetrics,
    onNavigatePatientSearch: () -> Unit,
    onNavigateQueue: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Clinical Consultation Workbench",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MetricCard(
                title = "Waiting for You",
                value = "${metrics.waitingQueueCount}",
                icon = Icons.Default.People,
                iconColor = MaterialTheme.colorScheme.primary,
                subtitle = "Queued in Room",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Pending Diagnostics",
                value = "${metrics.pendingLabOrders}",
                icon = Icons.Default.Biotech,
                iconColor = Color(0xFF0288D1),
                subtitle = "Lab orders ordered",
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onNavigateQueue,
                modifier = Modifier.weight(1f)
            ) {
                Text("Open Consultation Queue")
            }
            OutlinedButton(
                onClick = onNavigatePatientSearch,
                modifier = Modifier.weight(1f)
            ) {
                Text("Patient Directory")
            }
        }
    }
}

@Composable
private fun ReceptionMetricsSection(
    metrics: DashboardMetrics,
    onNavigatePatientSearch: () -> Unit,
    onNavigatePatientRegister: () -> Unit,
    onNavigateQueue: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Front-Desk Reception & Check-In",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MetricCard(
                title = "Waiting in Queue",
                value = "${metrics.waitingQueueCount}",
                icon = Icons.Default.ConfirmationNumber,
                iconColor = MaterialTheme.colorScheme.primary,
                subtitle = "Tickets issued",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Registered Patients",
                value = "${metrics.totalPatients}",
                icon = Icons.Default.People,
                iconColor = Color(0xFF2E7D32),
                subtitle = "Master index",
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Patient Reception & Registration Workflows",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onNavigatePatientSearch,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reception_search_patients_button")
                    ) {
                        Text("Search", fontSize = 13.sp)
                    }
                    Button(
                        onClick = onNavigatePatientRegister,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reception_register_patient_button")
                    ) {
                        Text("Register", fontSize = 13.sp)
                    }
                    Button(
                        onClick = onNavigateQueue,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Queue", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun NurseMetricsSection(
    metrics: DashboardMetrics,
    onNavigateQueue: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Triage & Nursing Care Station",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MetricCard(
                title = "Awaiting Triage",
                value = "${metrics.waitingQueueCount}",
                icon = Icons.Default.Assignment,
                iconColor = Color(0xFFE65100),
                subtitle = "Need vitals recorded",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Total Patients",
                value = "${metrics.totalPatients}",
                icon = Icons.Default.People,
                iconColor = MaterialTheme.colorScheme.primary,
                subtitle = "Active inpatient/OPD",
                modifier = Modifier.weight(1f)
            )
        }
        Button(
            onClick = onNavigateQueue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open Triage Queue & Vitals Station")
        }
    }
}

@Composable
private fun QueueOperatorMetricsSection(
    metrics: DashboardMetrics,
    onNavigateQueue: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Queue Dispatch Console",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MetricCard(
                title = "Currently Waiting",
                value = "${metrics.waitingQueueCount}",
                icon = Icons.Default.ConfirmationNumber,
                iconColor = Color(0xFFE65100),
                subtitle = "In waiting lounge",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "In Consultation",
                value = "${metrics.inConsultationCount}",
                icon = Icons.Default.CheckCircle,
                iconColor = Color(0xFF2E7D32),
                subtitle = "With physicians",
                modifier = Modifier.weight(1f)
            )
        }
        Button(
            onClick = onNavigateQueue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Launch Queue Dispatch Console")
        }
    }
}

@Composable
private fun LabMetricsSection(
    metrics: DashboardMetrics,
    onNavigateLaboratory: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Pathology & Diagnostics Laboratory",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MetricCard(
                title = "Pending Lab Tests",
                value = "${metrics.pendingLabOrders}",
                icon = Icons.Default.Biotech,
                iconColor = Color(0xFFE65100),
                subtitle = "Specimens & processing",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Physicians Active",
                value = "2",
                icon = Icons.Default.People,
                iconColor = MaterialTheme.colorScheme.primary,
                subtitle = "Requesting tests",
                modifier = Modifier.weight(1f)
            )
        }
        Button(
            onClick = onNavigateLaboratory,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open Laboratory Diagnostics Workstation")
        }
    }
}

@Composable
private fun PharmacyMetricsSection(
    metrics: DashboardMetrics,
    onNavigatePharmacy: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Central Hospital Pharmacy",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MetricCard(
                title = "Pending Prescriptions",
                value = "${metrics.pendingPrescriptions}",
                icon = Icons.Default.LocalPharmacy,
                iconColor = Color(0xFFE65100),
                subtitle = "Awaiting dispensing",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Medicine Catalog",
                value = "120+",
                icon = Icons.Default.Medication,
                iconColor = Color(0xFF2E7D32),
                subtitle = "Active batches tracked",
                modifier = Modifier.weight(1f)
            )
        }
        Button(
            onClick = onNavigatePharmacy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open Pharmacy Dispensing Console")
        }
    }
}

@Composable
private fun CashierMetricsSection(
    metrics: DashboardMetrics,
    onNavigateBilling: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Accounts & Cashier Terminal",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MetricCard(
                title = "Pending Invoices",
                value = "${metrics.pendingInvoices}",
                icon = Icons.Default.Assignment,
                iconColor = Color(0xFFE65100),
                subtitle = "Unpaid bills",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Receipts Collected",
                value = "$${metrics.totalRevenue.toInt()}",
                icon = Icons.Default.AttachMoney,
                iconColor = Color(0xFF2E7D32),
                subtitle = "Settled today",
                modifier = Modifier.weight(1f)
            )
        }
        Button(
            onClick = onNavigateBilling,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open Billing & Cashier Desk")
        }
    }
}

@Composable
private fun PatientPortalSection(
    metrics: DashboardMetrics,
    onNavigateProfile: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "My Patient Health Portal",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MetricCard(
                title = "My Appointments",
                value = "${metrics.todayAppointments}",
                icon = Icons.Default.CalendarMonth,
                iconColor = MaterialTheme.colorScheme.primary,
                subtitle = "Scheduled visits",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Queue Status",
                value = if (metrics.waitingQueueCount > 0) "#A-101" else "None",
                icon = Icons.Default.ConfirmationNumber,
                iconColor = Color(0xFF0288D1),
                subtitle = "Estimated wait: 15m",
                modifier = Modifier.weight(1f)
            )
        }
        Button(
            onClick = onNavigateProfile,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.People, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("View My Medical Profile & History")
        }
    }
}

@Composable
private fun LiveQueuePreviewCard(metrics: DashboardMetrics) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Live Outpatient Queue Tracker",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${metrics.waitingQueueCount} Waiting",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (metrics.activeTickets.isEmpty()) {
                Text(
                    text = "No active tickets in queue. Use Reception terminal to check in arriving patients.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                metrics.activeTickets.forEach { ticket ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Text(
                                    text = ticket.ticketNumber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Dept: ${ticket.departmentId.replace("dept_", "").uppercase()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Est. wait: ${ticket.estimatedWaitMinutes} mins",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        StatusChip(status = ticket.status)
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicalSafetyDisclaimerCard() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Medical Notice",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "CareFlow HMS is an operational workflow and decision-support system. Medical diagnoses and clinical decisions remain the sole responsibility of licensed healthcare practitioners.",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
