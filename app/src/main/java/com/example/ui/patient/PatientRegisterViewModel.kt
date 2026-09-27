package com.example.ui.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.model.AuthUser
import com.example.core.model.Patient
import com.example.core.model.PatientStatus
import com.example.core.util.PatientFormErrors
import com.example.core.util.PatientValidator
import com.example.core.util.Resource
import com.example.data.repository.AuthRepository
import com.example.data.repository.PatientRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class PatientRegisterFormState(
    // Personal Information
    val fullName: String = "",
    val dob: String = "", // YYYY-MM-DD
    val gender: String = "MALE",
    val nationalIdOrPassport: String = "",

    // Contact Information
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val preferredLanguage: String = "English",

    // Emergency Contact
    val emergencyContactName: String = "",
    val emergencyContactRelationship: String = "Spouse",
    val emergencyContactPhone: String = "",
    val emergencyContactSecondaryPhone: String = "",

    // Health Information
    val bloodGroup: String = "O+",
    val allergies: String = "",
    val existingConditions: String = "",

    // Status & UI
    val errors: PatientFormErrors = PatientFormErrors(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val matchedDuplicates: List<Patient> = emptyList(),
    val showDuplicateDialog: Boolean = false,
    val registeredPatient: Patient? = null
)

sealed class PatientRegisterEvent {
    data class RegistrationSuccess(val patient: Patient) : PatientRegisterEvent()
    data class NavigateToExistingProfile(val patientId: String) : PatientRegisterEvent()
}

class PatientRegisterViewModel(
    private val patientRepository: PatientRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<AuthUser?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _formState = MutableStateFlow(PatientRegisterFormState())
    val formState: StateFlow<PatientRegisterFormState> = _formState.asStateFlow()

    private val _events = MutableSharedFlow<PatientRegisterEvent>()
    val events: SharedFlow<PatientRegisterEvent> = _events.asSharedFlow()

    fun onFullNameChanged(value: String) {
        _formState.update { it.copy(fullName = value, errors = it.errors.copy(fullName = null)) }
    }

    fun onDobChanged(value: String) {
        _formState.update { it.copy(dob = value, errors = it.errors.copy(dob = null)) }
    }

    fun onGenderChanged(value: String) {
        _formState.update { it.copy(gender = value) }
    }

    fun onNationalIdChanged(value: String) {
        _formState.update { it.copy(nationalIdOrPassport = value) }
    }

    fun onPhoneChanged(value: String) {
        _formState.update { it.copy(phone = value, errors = it.errors.copy(phone = null)) }
    }

    fun onEmailChanged(value: String) {
        _formState.update { it.copy(email = value, errors = it.errors.copy(email = null)) }
    }

    fun onAddressChanged(value: String) {
        _formState.update { it.copy(address = value) }
    }

    fun onPreferredLanguageChanged(value: String) {
        _formState.update { it.copy(preferredLanguage = value) }
    }

    fun onEmergencyNameChanged(value: String) {
        _formState.update { it.copy(emergencyContactName = value, errors = it.errors.copy(emergencyContactName = null)) }
    }

    fun onEmergencyRelationshipChanged(value: String) {
        _formState.update { it.copy(emergencyContactRelationship = value) }
    }

    fun onEmergencyPhoneChanged(value: String) {
        _formState.update { it.copy(emergencyContactPhone = value, errors = it.errors.copy(emergencyContactPhone = null)) }
    }

    fun onEmergencySecondaryPhoneChanged(value: String) {
        _formState.update { it.copy(emergencyContactSecondaryPhone = value) }
    }

    fun onBloodGroupChanged(value: String) {
        _formState.update { it.copy(bloodGroup = value) }
    }

    fun onAllergiesChanged(value: String) {
        _formState.update { it.copy(allergies = value) }
    }

    fun onExistingConditionsChanged(value: String) {
        _formState.update { it.copy(existingConditions = value) }
    }

    fun dismissDuplicateDialog() {
        _formState.update { it.copy(showDuplicateDialog = false) }
    }

    fun submitRegistration() {
        val state = _formState.value
        val validationErrors = PatientValidator.validate(
            fullName = state.fullName,
            dob = state.dob,
            phone = state.phone,
            email = state.email,
            emergencyContactName = state.emergencyContactName,
            emergencyContactPhone = state.emergencyContactPhone
        )

        if (validationErrors.hasErrors) {
            _formState.update { it.copy(errors = validationErrors, errorMessage = "Please correct the highlighted validation errors") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, errorMessage = null) }

            // Step 1: Check for potential duplicates
            val duplicates = patientRepository.checkPossibleDuplicates(
                phone = state.phone,
                email = state.email,
                nationalId = state.nationalIdOrPassport,
                fullName = state.fullName,
                dob = state.dob
            )

            if (duplicates.isNotEmpty()) {
                _formState.update {
                    it.copy(
                        isLoading = false,
                        matchedDuplicates = duplicates,
                        showDuplicateDialog = true
                    )
                }
                return@launch
            }

            // Step 2: No duplicates, proceed to save directly
            proceedSave()
        }
    }

    fun forceProceedWithRegistration() {
        _formState.update { it.copy(showDuplicateDialog = false) }
        viewModelScope.launch {
            proceedSave()
        }
    }

    private suspend fun proceedSave() {
        val state = _formState.value
        _formState.update { it.copy(isLoading = true, errorMessage = null) }

        val newPatient = Patient(
            id = UUID.randomUUID().toString(),
            hospitalRegNo = "", // generated inside repo
            fullName = state.fullName.trim(),
            dob = state.dob.trim(),
            gender = state.gender,
            nationalIdOrPassport = state.nationalIdOrPassport.trim(),
            phone = state.phone.trim(),
            email = state.email.trim(),
            address = state.address.trim(),
            preferredLanguage = state.preferredLanguage,
            bloodGroup = state.bloodGroup,
            allergies = state.allergies.trim().ifBlank { "None reported" },
            existingConditions = state.existingConditions.trim().ifBlank { "None reported" },
            emergencyContactName = state.emergencyContactName.trim(),
            emergencyContactRelationship = state.emergencyContactRelationship.trim(),
            emergencyContactPhone = state.emergencyContactPhone.trim(),
            emergencyContactSecondaryPhone = state.emergencyContactSecondaryPhone.trim(),
            registeredDate = System.currentTimeMillis(),
            status = PatientStatus.ACTIVE,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            createdBy = "",
            updatedBy = ""
        )

        when (val result = patientRepository.registerPatient(newPatient)) {
            is Resource.Success -> {
                _formState.update { it.copy(isLoading = false, registeredPatient = result.data) }
                _events.emit(PatientRegisterEvent.RegistrationSuccess(result.data))
            }
            is Resource.Error -> {
                _formState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
            else -> Unit
        }
    }

    companion object {
        fun provideFactory(
            patientRepository: PatientRepository,
            authRepository: AuthRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PatientRegisterViewModel(patientRepository, authRepository) as T
                }
            }
    }
}
