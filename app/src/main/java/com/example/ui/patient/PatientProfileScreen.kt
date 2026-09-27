package com.example.ui.patient

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.core.model.PatientTimelineEvent
import com.example.core.util.DateTimeUtils
import com.example.ui.components.CareFlowScaffold
import com.example.ui.components.DemoEnvironmentBanner
import com.example.ui.components.StatusChip

@Composable
fun PatientProfileScreen(
    viewModel: PatientProfileViewModel,
    onNavigateEdit: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val patient by viewModel.patient.collectAsStateWithLifecycle()
    val timelineEvents by viewModel.timelineEvents.collectAsStateWithLifecycle()
    val departments by viewModel.departments.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    var showCheckInDialog by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }
    var archiveReason by remember { mutableStateOf("") }
    var generatedTicket by remember { mutableStateOf<String?>(null) }
    var infoToast by remember { mutableStateOf<String?>(null) }

    // Check-in form states
    var selectedDeptId by remember { mutableStateOf("dept_gen_med") }
    var selectedPriority by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PatientProfileEvent.TicketGenerated -> {
                    showCheckInDialog = false
                    generatedTicket = event.ticketNumber
                }
                is PatientProfileEvent.ShowToast -> {
                    infoToast = event.message
                }
            }
        }
    }

    CareFlowScaffold(
        title = "Patient Master Record",
        currentUser = currentUser,
        canNavigateBack = true,
        onNavigateBack = onNavigateBack
    ) {
        if (patient == null) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
        } else {
            val p = patient!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Profile Card
                item {
                    PatientHeaderCard(
                        patient = p,
                        canEdit = viewModel.canEditDemographics(),
                        canCheckIn = viewModel.canManageQueue(),
                        canArchive = viewModel.canArchivePatient(),
                        onEditClick = { onNavigateEdit(p.id) },
                        onCheckInClick = { showCheckInDialog = true },
                        onArchiveClick = { showArchiveDialog = true }
                    )
                }

                // Profile Navigation Tabs
                item {
                    val tabs = if (viewModel.canViewClinicalTimeline()) {
                        listOf("Demographics", "Clinical Timeline", "Events")
                    } else {
                        listOf("Demographics", "Administrative Events")
                    }

                    TabRow(
                        selectedTabIndex = selectedTab.coerceAtMost(tabs.lastIndex),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { viewModel.selectTab(index) },
                                text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                            )
                        }
                    }
                }

                // Tab Content
                if (selectedTab == 0) {
                    item { DemographicsTab(patient = p) }
                } else if (selectedTab == 1 && viewModel.canViewClinicalTimeline()) {
                    if (timelineEvents.isEmpty()) {
                        item {
                            EmptyTimelineCard(message = "No clinical encounters or triage vitals recorded for this patient yet.")
                        }
                    } else {
                        items(timelineEvents, key = { it.id }) { event ->
                            TimelineEventCard(event = event)
                        }
                    }
                } else {
                    // Non-clinical / Administrative events
                    val adminEvents = timelineEvents.filter { !it.isConfidentialClinical }
                    if (adminEvents.isEmpty()) {
                        item {
                            EmptyTimelineCard(message = "No administrative events logged yet.")
                        }
                    } else {
                        items(adminEvents, key = { it.id }) { event ->
                            TimelineEventCard(event = event)
                        }
                    }
                }
            }
        }

        // Check In Dialog
        if (showCheckInDialog) {
            AlertDialog(
                onDismissRequest = { showCheckInDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Queue Check-In Dispatch", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Assign ${patient?.fullName} to a hospital department queue:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Department *", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        departments.forEach { dept ->
                            FilterChip(
                                selected = selectedDeptId == dept.id,
                                onClick = { selectedDeptId = dept.id },
                                label = { Text("${dept.name} (${dept.code})") },
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Triage Priority *", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedPriority == 0,
                                onClick = { selectedPriority = 0 },
                                label = { Text("Standard") }
                            )
                            FilterChip(
                                selected = selectedPriority == 1,
                                onClick = { selectedPriority = 1 },
                                label = { Text("Urgent") }
                            )
                            FilterChip(
                                selected = selectedPriority == 2,
                                onClick = { selectedPriority = 2 },
                                label = { Text("Emergency") }
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.checkInPatient(selectedDeptId, null, selectedPriority) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Issue Queue Ticket")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCheckInDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Generated Ticket Confirmation Modal
        generatedTicket?.let { ticketNumber ->
            AlertDialog(
                onDismissRequest = { generatedTicket = null },
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(42.dp)
                    )
                },
                title = { Text("Queue Ticket Generated", fontWeight = FontWeight.Bold) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Patient is checked in and queued for consultation:")
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = ticketNumber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ticket dispatched to Queue Console and Waiting Area display.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { generatedTicket = null }) {
                        Text("Done")
                    }
                }
            )
        }

        // Archive Confirmation Dialog
        if (showArchiveDialog) {
            AlertDialog(
                onDismissRequest = { showArchiveDialog = false },
                title = { Text("Confirm Patient Archival") },
                text = {
                    Column {
                        Text("Archiving marks the patient record inactive. It is preserved for audit trails but hidden from standard active queues.")
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = archiveReason,
                            onValueChange = { archiveReason = it },
                            label = { Text("Reason for Archival *") },
                            placeholder = { Text("e.g. Transferred to another hospital") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (archiveReason.isNotBlank()) {
                                viewModel.archivePatient(archiveReason)
                                showArchiveDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Archive Patient Record")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showArchiveDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun PatientHeaderCard(
    patient: Patient,
    canEdit: Boolean,
    canCheckIn: Boolean,
    canArchive: Boolean,
    onEditClick: () -> Unit,
    onCheckInClick: () -> Unit,
    onArchiveClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = patient.fullName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = patient.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hospital ID: ${patient.hospitalRegNo}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                StatusChip(status = patient.status.name)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Demographics summary pill badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${patient.gender.lowercase().replaceFirstChar { it.uppercase() }} • ${patient.age} yrs (${patient.dob})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Blood: ${patient.bloodGroup}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (canCheckIn) {
                    Button(
                        onClick = onCheckInClick,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("button_patient_check_in")
                    ) {
                        Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Check In", fontSize = 13.sp)
                    }
                }

                if (canEdit) {
                    OutlinedButton(
                        onClick = onEditClick,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("button_edit_patient")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit", fontSize = 13.sp)
                    }
                }

                if (canArchive && patient.status.name != "ARCHIVED") {
                    OutlinedButton(
                        onClick = onArchiveClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DemographicsTab(patient: Patient) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Contact & Demographics", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                InfoRow(label = "Phone Number", value = patient.phone)
                InfoRow(label = "Email Address", value = patient.email.ifBlank { "Not provided" })
                InfoRow(label = "Residential Address", value = patient.address.ifBlank { "Not provided" })
                InfoRow(label = "Preferred Language", value = patient.preferredLanguage)
                InfoRow(label = "National ID / Passport", value = patient.nationalIdOrPassport.ifBlank { "Not provided" })
                InfoRow(label = "Registration Date", value = DateTimeUtils.formatDate(patient.registeredDate))
            }
        }

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Primary Emergency Contact", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                InfoRow(label = "Contact Person", value = "${patient.emergencyContactName} (${patient.emergencyContactRelationship})")
                InfoRow(label = "Primary Phone", value = patient.emergencyContactPhone)
                if (patient.emergencyContactSecondaryPhone.isNotBlank()) {
                    InfoRow(label = "Secondary Phone", value = patient.emergencyContactSecondaryPhone)
                }
            }
        }

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Clinical Baseline Alerts", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                InfoRow(label = "Known Allergies", value = patient.allergies, isAlert = patient.allergies != "None reported")
                InfoRow(label = "Existing Conditions", value = patient.existingConditions)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, isAlert: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TimelineEventCard(event: PatientTimelineEvent) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (event.isConfidentialClinical) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Clinical Record",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                StatusChip(status = event.status)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = event.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${event.department} • ${event.practitionerName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = DateTimeUtils.formatFullDateTime(event.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun EmptyTimelineCard(message: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(32.dp).fillMaxWidth()) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
