package com.example.ui.patient

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.Patient
import com.example.ui.components.CareFlowScaffold

@Composable
fun PatientRegisterScreen(
    viewModel: PatientRegisterViewModel,
    onNavigateProfile: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    var successPatient by remember { mutableStateOf<Patient?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PatientRegisterEvent.RegistrationSuccess -> {
                    successPatient = event.patient
                }
                is PatientRegisterEvent.NavigateToExistingProfile -> {
                    onNavigateProfile(event.patientId)
                }
            }
        }
    }

    CareFlowScaffold(
        title = "New Patient Registration",
        currentUser = currentUser,
        canNavigateBack = true,
        onNavigateBack = onNavigateBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Header Info
            Text(
                text = "Hospital Admitting & Master Record Creation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Fields marked with an asterisk (*) are mandatory. Ensure patient consent is recorded.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (formState.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(12.dp)
                ) {
                    Text(
                        text = formState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // SECTION 1: Personal Information
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "1. Personal Information",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = formState.fullName,
                        onValueChange = viewModel::onFullNameChanged,
                        label = { Text("Full Legal Name *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        isError = formState.errors.fullName != null,
                        supportingText = formState.errors.fullName?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_patient_name")
                    )

                    OutlinedTextField(
                        value = formState.dob,
                        onValueChange = viewModel::onDobChanged,
                        label = { Text("Date of Birth * (YYYY-MM-DD)") },
                        placeholder = { Text("e.g. 1988-04-15") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        isError = formState.errors.dob != null,
                        supportingText = formState.errors.dob?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_patient_dob")
                    )

                    Column {
                        Text(
                            text = "Gender *",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("MALE" to "Male", "FEMALE" to "Female", "OTHER" to "Other").forEach { (key, label) ->
                                FilterChip(
                                    selected = formState.gender == key,
                                    onClick = { viewModel.onGenderChanged(key) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = formState.nationalIdOrPassport,
                        onValueChange = viewModel::onNationalIdChanged,
                        label = { Text("National ID / Passport (Optional)") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // SECTION 2: Contact Information
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "2. Contact Information",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = formState.phone,
                        onValueChange = viewModel::onPhoneChanged,
                        label = { Text("Primary Phone Number *") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        isError = formState.errors.phone != null,
                        supportingText = formState.errors.phone?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_patient_phone")
                    )

                    OutlinedTextField(
                        value = formState.email,
                        onValueChange = viewModel::onEmailChanged,
                        label = { Text("Email Address (Optional)") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        isError = formState.errors.email != null,
                        supportingText = formState.errors.email?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = formState.address,
                        onValueChange = viewModel::onAddressChanged,
                        label = { Text("Residential Address") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                        singleLine = false,
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column {
                        Text(
                            text = "Preferred Language",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("English", "Spanish", "French", "Other").forEach { lang ->
                                FilterChip(
                                    selected = formState.preferredLanguage == lang,
                                    onClick = { viewModel.onPreferredLanguageChanged(lang) },
                                    label = { Text(lang) }
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 3: Emergency Contact
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "3. Emergency Contact",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = formState.emergencyContactName,
                        onValueChange = viewModel::onEmergencyNameChanged,
                        label = { Text("Emergency Contact Name *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        isError = formState.errors.emergencyContactName != null,
                        supportingText = formState.errors.emergencyContactName?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column {
                        Text(
                            text = "Relationship to Patient",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Spouse", "Parent", "Child", "Sibling", "Friend").forEach { rel ->
                                FilterChip(
                                    selected = formState.emergencyContactRelationship == rel,
                                    onClick = { viewModel.onEmergencyRelationshipChanged(rel) },
                                    label = { Text(rel) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = formState.emergencyContactPhone,
                        onValueChange = viewModel::onEmergencyPhoneChanged,
                        label = { Text("Emergency Contact Phone *") },
                        leadingIcon = { Icon(Icons.Default.ContactPhone, contentDescription = null) },
                        isError = formState.errors.emergencyContactPhone != null,
                        supportingText = formState.errors.emergencyContactPhone?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = formState.emergencyContactSecondaryPhone,
                        onValueChange = viewModel::onEmergencySecondaryPhoneChanged,
                        label = { Text("Secondary Contact Phone (Optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // SECTION 4: Health Information (Optional / Intake)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "4. Baseline Clinical Intake (Optional)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Column {
                        Text(
                            text = "Blood Group",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-").forEach { bg ->
                                FilterChip(
                                    selected = formState.bloodGroup == bg,
                                    onClick = { viewModel.onBloodGroupChanged(bg) },
                                    label = { Text(bg) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = formState.allergies,
                        onValueChange = viewModel::onAllergiesChanged,
                        label = { Text("Known Allergies (e.g. Penicillin, Latex)") },
                        leadingIcon = { Icon(Icons.Default.Medication, contentDescription = null) },
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = formState.existingConditions,
                        onValueChange = viewModel::onExistingConditionsChanged,
                        label = { Text("Existing Chronic Conditions (e.g. Hypertension)") },
                        leadingIcon = { Icon(Icons.Default.LocalHospital, contentDescription = null) },
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Button(
                onClick = viewModel::submitRegistration,
                enabled = !formState.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("button_submit_registration")
            ) {
                if (formState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Complete Registration & Issue Patient ID", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Possible Duplicate Detection Dialog
        if (formState.showDuplicateDialog) {
            DuplicatePatientDialog(
                matchedPatients = formState.matchedDuplicates,
                onViewExisting = { existing ->
                    viewModel.dismissDuplicateDialog()
                    onNavigateProfile(existing.id)
                },
                onContinueRegistration = {
                    viewModel.forceProceedWithRegistration()
                },
                onDismiss = {
                    viewModel.dismissDuplicateDialog()
                }
            )
        }

        // Registration Success Modal Dialog
        successPatient?.let { patient ->
            AlertDialog(
                onDismissRequest = { /* forced choice */ },
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(40.dp)
                    )
                },
                title = {
                    Text(
                        text = "Patient Registered Successfully",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "New master record created for ${patient.fullName}.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Hospital Registration ID",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = patient.hospitalRegNo,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "What would you like to do next for this patient?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            successPatient = null
                            onNavigateProfile(patient.id)
                        }
                    ) {
                        Text("View Profile & Intake")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            successPatient = null
                            onNavigateBack()
                        }
                    ) {
                        Text("Back to Directory")
                    }
                }
            )
        }
    }
}
