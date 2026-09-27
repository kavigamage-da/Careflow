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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.PatientStatus
import com.example.ui.components.DemoEnvironmentBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientEditScreen(
    viewModel: PatientEditViewModel,
    onSaveSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.saveSuccessEvent.collect {
            onSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            Column {
                DemoEnvironmentBanner()
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Edit Patient Record",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (state.fullName.isNotBlank()) {
                                Text(
                                    text = "${state.fullName} • ${state.hospitalRegNo}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("patient_edit_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate Back")
                        }
                    },
                    actions = {
                        Button(
                            onClick = { viewModel.saveChanges() },
                            enabled = !state.isLoading,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .testTag("patient_edit_save_button")
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Changes")
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error banner if any
            state.errorMessage?.let { error ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Locked Identity & Legal Demographics
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "CORE IDENTIFIERS (READ-ONLY)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Locked for Compliance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Full Legal Name", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(state.fullName.ifBlank { "N/A" }, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Hospital Reg. ID", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(state.hospitalRegNo.ifBlank { "N/A" }, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Date of Birth", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(state.dob.ifBlank { "N/A" }, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Gender", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(state.gender.ifBlank { "N/A" }, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        if (state.nationalIdOrPassport.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("National ID / Passport", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(state.nationalIdOrPassport, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Editable Contact Information
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "CONTACT INFORMATION",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = state.phone,
                            onValueChange = { viewModel.onPhoneChanged(it) },
                            label = { Text("Primary Phone Number *") },
                            isError = state.errors.phone != null,
                            supportingText = { state.errors.phone?.let { Text(it) } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_phone_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.email,
                            onValueChange = { viewModel.onEmailChanged(it) },
                            label = { Text("Email Address *") },
                            isError = state.errors.email != null,
                            supportingText = { state.errors.email?.let { Text(it) } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_email_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.address,
                            onValueChange = { viewModel.onAddressChanged(it) },
                            label = { Text("Residential Address") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_address_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.preferredLanguage,
                            onValueChange = { viewModel.onLanguageChanged(it) },
                            label = { Text("Preferred Language") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_language_input")
                        )
                    }
                }
            }

            // Editable Emergency Contact
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "EMERGENCY CONTACT",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = state.emergencyContactName,
                            onValueChange = { viewModel.onEmergencyNameChanged(it) },
                            label = { Text("Emergency Contact Name *") },
                            isError = state.errors.emergencyContactName != null,
                            supportingText = { state.errors.emergencyContactName?.let { Text(it) } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_emergency_name_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.emergencyContactRelationship,
                            onValueChange = { viewModel.onEmergencyRelChanged(it) },
                            label = { Text("Relationship (e.g. Spouse, Parent, Sibling)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_emergency_relationship_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.emergencyContactPhone,
                            onValueChange = { viewModel.onEmergencyPhoneChanged(it) },
                            label = { Text("Emergency Contact Phone *") },
                            isError = state.errors.emergencyContactPhone != null,
                            supportingText = { state.errors.emergencyContactPhone?.let { Text(it) } },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_emergency_phone_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.emergencyContactSecondaryPhone,
                            onValueChange = { viewModel.onEmergencySecPhoneChanged(it) },
                            label = { Text("Secondary Emergency Phone (Optional)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_emergency_sec_phone_input")
                        )
                    }
                }
            }

            // Editable Clinical Conditions & Allergies
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "HEALTH & CLINICAL NOTES",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = state.allergies,
                            onValueChange = { viewModel.onAllergiesChanged(it) },
                            label = { Text("Known Allergies (Food, Meds, Latex)") },
                            placeholder = { Text("e.g. Penicillin, Peanuts, None") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_allergies_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.existingConditions,
                            onValueChange = { viewModel.onConditionsChanged(it) },
                            label = { Text("Existing Medical Conditions / Comorbidities") },
                            placeholder = { Text("e.g. Hypertension, Type 2 Diabetes, Asthma") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_conditions_input")
                        )
                    }
                }
            }

            // Administrative Status Change (Super Admin / Hospital Admin only)
            if (viewModel.canArchive()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "ADMINISTRATIVE RECORD STATUS",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                PatientStatus.values().forEach { s ->
                                    FilterChip(
                                        selected = state.status == s,
                                        onClick = { viewModel.onStatusChanged(s) },
                                        label = { Text(s.displayName, fontSize = 12.sp) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Save Action
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.saveChanges() },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("patient_edit_save_bottom_button")
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Demographic Updates", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
