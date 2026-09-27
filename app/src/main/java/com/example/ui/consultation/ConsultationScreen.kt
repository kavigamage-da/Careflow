package com.example.ui.consultation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CareFlowScaffold

@Composable
fun ConsultationScreen(
    viewModel: ConsultationViewModel,
    onNavigateBack: () -> Unit,
    onNavigateAuditLogs: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(0) }
    var showAddDrugDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onNavigateBack()
        }
    }

    CareFlowScaffold(
        title = "Doctor Consultation",
        canNavigateBack = true,
        onNavigateBack = onNavigateBack,
        onNavigateAuditLogs = onNavigateAuditLogs,
        onNavigateSettings = onNavigateSettings
    ) {
        if (uiState.isLoading && uiState.ticket == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Patient & Vitals Summary Card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = uiState.patient?.fullName ?: "Patient",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "MRN: ${uiState.patient?.hospitalRegNo ?: "N/A"} • Ticket: ${uiState.ticket?.ticketNumber ?: "N/A"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!uiState.patient?.allergies.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFFEBEE), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Allergy: ${uiState.patient?.allergies}",
                                        color = Color(0xFFC62828),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Triage Vitals Strip
                        uiState.latestVitals?.let { vitals ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                VitalsChip(label = "BP", value = "${vitals.systolicBp ?: "--"}/${vitals.diastolicBp ?: "--"}")
                                VitalsChip(label = "Pulse", value = "${vitals.pulseRateBpm ?: "--"} bpm")
                                VitalsChip(label = "Temp", value = "${vitals.temperatureCelsius ?: "--"} °C")
                                VitalsChip(label = "SpO2", value = "${vitals.oxygenSaturationPercent ?: "--"}%")
                            }
                        }
                    }
                }

                // Section Tabs
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Clinical Notes", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.NoteAlt, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Prescription (${uiState.prescribedDrugs.size})", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Medication, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Lab Orders (${uiState.selectedLabTests.size})", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Biotech, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                when (selectedTab) {
                    0 -> {
                        // Clinical Notes Tab
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = uiState.chiefComplaint,
                                    onValueChange = { viewModel.updateChiefComplaint(it) },
                                    label = { Text("Chief Complaint *") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("chief_complaint_input"),
                                    maxLines = 2
                                )
                                OutlinedTextField(
                                    value = uiState.historyOfPresentIllness,
                                    onValueChange = { viewModel.updateHistory(it) },
                                    label = { Text("History of Present Illness") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3
                                )
                                OutlinedTextField(
                                    value = uiState.examinationNotes,
                                    onValueChange = { viewModel.updateExamination(it) },
                                    label = { Text("Physical Examination Findings") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3
                                )
                                OutlinedTextField(
                                    value = uiState.clinicianDiagnosis,
                                    onValueChange = { viewModel.updateDiagnosis(it) },
                                    label = { Text("Clinician Diagnosis Documentation *") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("clinician_diagnosis_input"),
                                    maxLines = 2
                                )
                                OutlinedTextField(
                                    value = uiState.treatmentPlan,
                                    onValueChange = { viewModel.updateTreatmentPlan(it) },
                                    label = { Text("Management & Treatment Plan") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3
                                )
                                OutlinedTextField(
                                    value = uiState.followUpInstructions,
                                    onValueChange = { viewModel.updateFollowUp(it) },
                                    label = { Text("Follow-up Instructions") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                    1 -> {
                        // Prescriptions Tab
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Prescription Items",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Button(
                                        onClick = { showAddDrugDialog = true },
                                        modifier = Modifier.testTag("add_medicine_button")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Drug")
                                    }
                                }

                                if (uiState.prescribedDrugs.isEmpty()) {
                                    Text(
                                        text = "No medications added. Click 'Add Drug' to prescribe medicines.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                } else {
                                    uiState.prescribedDrugs.forEachIndexed { index, drug ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(drug.name, fontWeight = FontWeight.Bold)
                                                Text(
                                                    "${drug.dosage} • ${drug.frequency} • ${drug.duration}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Text(
                                                    drug.instructions,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            IconButton(onClick = { viewModel.removePrescriptionDrug(index) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color(0xFFC62828))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        // Lab Orders Tab
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Select Laboratory Investigations",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                val commonTests = listOf(
                                    "Complete Blood Count (CBC)",
                                    "Fasting Blood Sugar (FBS)",
                                    "Lipid Profile",
                                    "Renal Function Test (RFT)",
                                    "Liver Function Test (LFT)",
                                    "Urinalysis",
                                    "Serum Electrolytes",
                                    "HbA1c"
                                )

                                commonTests.forEach { testName ->
                                    val isSelected = uiState.selectedLabTests.contains(testName)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(testName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { viewModel.toggleLabTest(testName) },
                                            label = { Text(if (isSelected) "Selected" else "Add") }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Order Priority", fontWeight = FontWeight.SemiBold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("ROUTINE", "URGENT", "STAT").forEach { priority ->
                                        FilterChip(
                                            selected = uiState.labOrderPriority == priority,
                                            onClick = { viewModel.setLabPriority(priority) },
                                            label = { Text(priority) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Finalize Action
                Button(
                    onClick = { viewModel.finalizeConsultation() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("finalize_consultation_button"),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Finalize Consultation & Dispatch Orders")
                    }
                }
            }
        }
    }

    // Add Drug Dialog
    if (showAddDrugDialog) {
        AddDrugDialog(
            onDismiss = { showAddDrugDialog = false },
            onAdd = { drug ->
                viewModel.addPrescriptionDrug(drug)
                showAddDrugDialog = false
            }
        )
    }
}

@Composable
private fun VitalsChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AddDrugDialog(
    onDismiss: () -> Unit,
    onAdd: (PrescribedDrug) -> Unit
) {
    var drugName by remember { mutableStateOf("Amoxicillin 500mg") }
    var dosage by remember { mutableStateOf("1 capsule") }
    var frequency by remember { mutableStateOf("TDS (3 times daily)") }
    var duration by remember { mutableStateOf("5 days") }
    var instructions by remember { mutableStateOf("Take with or after meals") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Prescribe Medication") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = drugName,
                    onValueChange = { drugName = it },
                    label = { Text("Medication Name & Strength") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage (e.g. 1 tab, 10ml)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = frequency,
                    onValueChange = { frequency = it },
                    label = { Text("Frequency (e.g. BD, TDS, QDS)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (e.g. 5 days, 1 month)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Special Instructions") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (drugName.isNotBlank()) {
                        onAdd(PrescribedDrug(drugName, dosage, frequency, duration, instructions))
                    }
                }
            ) {
                Text("Add to Prescription")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
