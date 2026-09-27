package com.example.ui.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.Patient
import com.example.core.model.QueuePriority
import com.example.core.model.QueueStatus
import com.example.core.model.QueueTicket
import com.example.core.model.UserRole
import com.example.ui.components.CareFlowScaffold
import com.example.ui.components.MetricCard
import com.example.ui.components.StatusChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    viewModel: QueueViewModel,
    onNavigateBack: () -> Unit,
    onNavigateAuditLogs: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateTriage: (String) -> Unit = {},
    onNavigateConsultation: (String) -> Unit = {},
    onNavigatePatientProfile: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var ticketForPriorityDialog by remember { mutableStateOf<QueueTicket?>(null) }
    var ticketForTransferDialog by remember { mutableStateOf<QueueTicket?>(null) }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    CareFlowScaffold(
        title = "Queue Operations",
        canNavigateBack = true,
        onNavigateBack = onNavigateBack,
        onNavigateAuditLogs = onNavigateAuditLogs,
        onNavigateSettings = onNavigateSettings,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openCheckInDialog() },
                modifier = Modifier.testTag("check_in_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Check-In Patient")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Check In", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Metrics and Call Next banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Live Queue Flow",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Active tickets: ${uiState.tickets.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Call Next Button
                        Button(
                            onClick = { viewModel.callNext() },
                            modifier = Modifier.testTag("call_next_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call Next Patient")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Metrics Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val waitingCount = uiState.tickets.count { it.status == QueueStatus.WAITING }
                        val calledCount = uiState.tickets.count { it.status == QueueStatus.CALLED }
                        val inConsultCount = uiState.tickets.count { it.status == QueueStatus.IN_CONSULTATION }

                        QueueMiniKpi(label = "Waiting", count = waitingCount, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                        QueueMiniKpi(label = "Called", count = calledCount, color = Color(0xFFE65100), modifier = Modifier.weight(1f))
                        QueueMiniKpi(label = "In Consult", count = inConsultCount, color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
                    }
                }
            }

            // Department Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val departments = listOf(
                    "ALL" to "All Departments",
                    "dept_gen_med" to "General Medicine",
                    "dept_cardio" to "Cardiology",
                    "dept_pediatrics" to "Pediatrics",
                    "dept_ortho" to "Orthopedics",
                    "dept_emergency" to "Emergency"
                )

                departments.forEach { (id, label) ->
                    FilterChip(
                        selected = uiState.selectedDepartment == id,
                        onClick = { viewModel.setDepartment(id) },
                        label = { Text(label) }
                    )
                }
            }

            // Status Filter Tabs
            val statusTabs = listOf(
                "ACTIVE" to "Active (${uiState.tickets.count { it.status in setOf(QueueStatus.WAITING, QueueStatus.CALLED, QueueStatus.IN_CONSULTATION) }})",
                "WAITING" to "Waiting (${uiState.tickets.count { it.status == QueueStatus.WAITING }})",
                "CALLED" to "Called (${uiState.tickets.count { it.status == QueueStatus.CALLED }})",
                "IN_CONSULTATION" to "In Consult (${uiState.tickets.count { it.status == QueueStatus.IN_CONSULTATION }})"
            )

            val currentTabIndex = statusTabs.indexOfFirst { it.first == uiState.selectedStatusFilter }.coerceAtLeast(0)

            TabRow(
                selectedTabIndex = currentTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                statusTabs.forEachIndexed { index, (key, title) ->
                    Tab(
                        selected = currentTabIndex == index,
                        onClick = { viewModel.setStatusFilter(key) },
                        text = { Text(title, fontSize = 12.sp) }
                    )
                }
            }

            // Ticket List
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredTickets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.HourglassTop,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No tickets in this view",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "New tickets will appear here once patients check in.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredTickets, key = { it.id }) { ticket ->
                        QueueTicketCard(
                            ticket = ticket,
                            currentUserRole = uiState.currentUserRole,
                            onRecall = { viewModel.recall(ticket.id) },
                            onStartConsult = {
                                viewModel.startConsultation(ticket.id)
                                onNavigateConsultation(ticket.id)
                            },
                            onTriage = { onNavigateTriage(ticket.id) },
                            onComplete = { viewModel.completeConsultation(ticket.id) },
                            onSkip = { viewModel.skipTicket(ticket.id) },
                            onChangePriority = { ticketForPriorityDialog = ticket },
                            onTransfer = { ticketForTransferDialog = ticket },
                            onViewPatient = { onNavigatePatientProfile(ticket.patientId) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    // Check-in Dialog
    if (uiState.isCheckInDialogOpen) {
        CheckInDialog(
            uiState = uiState,
            onDismiss = { viewModel.closeCheckInDialog() },
            onSearchPatient = { viewModel.searchPatientForCheckIn(it) },
            onSelectPatient = { viewModel.selectPatientForCheckIn(it) },
            onDepartmentChange = { viewModel.setCheckInDepartment(it) },
            onPriorityChange = { viewModel.setCheckInPriority(it) },
            onNotesChange = { viewModel.setCheckInNotes(it) },
            onSubmit = { viewModel.submitCheckIn() }
        )
    }

    // Priority Change Dialog
    ticketForPriorityDialog?.let { ticket ->
        ChangePriorityDialog(
            ticket = ticket,
            onDismiss = { ticketForPriorityDialog = null },
            onConfirm = { newPriority ->
                viewModel.updatePriority(ticket.id, newPriority)
                ticketForPriorityDialog = null
            }
        )
    }

    // Department Transfer Dialog
    ticketForTransferDialog?.let { ticket ->
        TransferDepartmentDialog(
            ticket = ticket,
            onDismiss = { ticketForTransferDialog = null },
            onConfirm = { targetDept, reason ->
                viewModel.transferDepartment(ticket.id, targetDept, reason)
                ticketForTransferDialog = null
            }
        )
    }
}

@Composable
private fun QueueMiniKpi(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun QueueTicketCard(
    ticket: QueueTicket,
    currentUserRole: UserRole?,
    onRecall: () -> Unit,
    onStartConsult: () -> Unit,
    onTriage: () -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    onChangePriority: () -> Unit,
    onTransfer: () -> Unit,
    onViewPatient: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ticket_card_${ticket.ticketNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Ticket #, Priority Chip, Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = ticket.ticketNumber,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    PriorityBadge(priority = ticket.priority)
                }

                StatusChip(status = ticket.status.displayName)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Patient Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onViewPatient() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = ticket.patientName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "MRN: ${ticket.patientRegNo.ifBlank { "N/A" }} • ${ticket.departmentName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!ticket.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Notes: ${ticket.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Contextual Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (ticket.status) {
                    QueueStatus.WAITING -> {
                        OutlinedButton(
                            onClick = onChangePriority,
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text("Priority", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = onTransfer,
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text("Transfer", fontSize = 12.sp)
                        }
                        Button(
                            onClick = onRecall,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Call", fontSize = 12.sp)
                        }
                    }
                    QueueStatus.CALLED -> {
                        OutlinedButton(
                            onClick = onSkip,
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text("Skip", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = onRecall,
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text("Recall (${ticket.recallCount})", fontSize = 12.sp)
                        }
                        if (currentUserRole == UserRole.NURSE) {
                            Button(
                                onClick = onTriage,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                            ) {
                                Text("Triage Vitals", fontSize = 12.sp)
                            }
                        } else {
                            Button(
                                onClick = onStartConsult,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Start Consult", fontSize = 12.sp)
                            }
                        }
                    }
                    QueueStatus.IN_CONSULTATION -> {
                        Button(
                            onClick = onStartConsult,
                            modifier = Modifier.padding(end = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Resume Consult", fontSize = 12.sp)
                        }
                        Button(
                            onClick = onComplete,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text("Complete", fontSize = 12.sp)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun PriorityBadge(priority: QueuePriority) {
    val (bg, fg) = when (priority) {
        QueuePriority.EMERGENCY -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        QueuePriority.URGENT -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        QueuePriority.FAST_TRACK -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        QueuePriority.STANDARD -> Color(0xFFF5F5F5) to Color(0xFF616161)
    }

    Box(
        modifier = Modifier
            .background(color = bg, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = priority.displayName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}

@Composable
fun CheckInDialog(
    uiState: QueueUiState,
    onDismiss: () -> Unit,
    onSearchPatient: (String) -> Unit,
    onSelectPatient: (Patient) -> Unit,
    onDepartmentChange: (String) -> Unit,
    onPriorityChange: (QueuePriority) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Patient Check-In & Queue Ticket",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Patient Selection / Search
                Text("Select Patient", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = uiState.checkInPatientQuery,
                    onValueChange = onSearchPatient,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search name, phone, or MRN") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )

                // Search Results Dropdown List
                if (uiState.checkInMatchingPatients.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            uiState.checkInMatchingPatients.take(3).forEach { patient ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectPatient(patient) }
                                        .padding(vertical = 6.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(patient.fullName, fontWeight = FontWeight.SemiBold)
                                        Text(patient.hospitalRegNo, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Icon(Icons.Default.Check, contentDescription = "Select", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                // Department
                Text("Target Department", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                val depts = listOf(
                    "dept_gen_med" to "General Medicine",
                    "dept_cardio" to "Cardiology",
                    "dept_pediatrics" to "Pediatrics",
                    "dept_ortho" to "Orthopedics",
                    "dept_emergency" to "Emergency"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    depts.forEach { (id, label) ->
                        FilterChip(
                            selected = uiState.checkInDepartmentId == id,
                            onClick = { onDepartmentChange(id) },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }

                // Priority
                Text("Priority Level", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QueuePriority.values().forEach { priority ->
                        FilterChip(
                            selected = uiState.checkInPriority == priority,
                            onClick = { onPriorityChange(priority) },
                            label = { Text(priority.displayName, fontSize = 12.sp) }
                        )
                    }
                }

                // Notes
                OutlinedTextField(
                    value = uiState.checkInNotes,
                    onValueChange = onNotesChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Chief Complaint / Notes (Optional)") },
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = uiState.selectedPatientForCheckIn != null && !uiState.isLoading,
                modifier = Modifier.testTag("submit_check_in_button")
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Issue Ticket")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ChangePriorityDialog(
    ticket: QueueTicket,
    onDismiss: () -> Unit,
    onConfirm: (QueuePriority) -> Unit
) {
    var selectedPriority by remember { mutableStateOf(ticket.priority) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Ticket Priority: ${ticket.ticketNumber}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Patient: ${ticket.patientName}", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                QueuePriority.values().forEach { priority ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPriority = priority }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(priority.displayName, fontWeight = FontWeight.Medium)
                            Text(priority.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedPriority) }) {
                Text("Save Priority")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TransferDepartmentDialog(
    ticket: QueueTicket,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var selectedDept by remember { mutableStateOf("dept_gen_med") }
    var reason by remember { mutableStateOf("Consultant referral") }

    val depts = listOf(
        "dept_gen_med" to "General Medicine",
        "dept_cardio" to "Cardiology",
        "dept_pediatrics" to "Pediatrics",
        "dept_ortho" to "Orthopedics",
        "dept_emergency" to "Emergency"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transfer Ticket: ${ticket.ticketNumber}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Current: ${ticket.departmentName}")
                Text("Target Department:", fontWeight = FontWeight.SemiBold)
                depts.forEach { (id, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedDept = id }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedDept == id,
                            onClick = { selectedDept = id }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(name)
                    }
                }
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for transfer") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedDept, reason) }) {
                Text("Confirm Transfer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
