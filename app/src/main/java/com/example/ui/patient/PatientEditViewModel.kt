package com.example.ui.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.model.AuthUser
import com.example.core.model.Patient
import com.example.core.model.PatientStatus
import com.example.core.model.Permission
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

data class PatientEditState(
    val initialLoaded: Boolean = false,
    val patientId: String = "",
    val hospitalRegNo: String = "",
    val fullName: String = "",
    val dob: String = "",
    val gender: String = "",
    val nationalIdOrPassport: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val preferredLanguage: String = "English",
    val bloodGroup: String = "O+",
    val allergies: String = "",
    val existingConditions: String = "",
    val emergencyContactName: String = "",
    val emergencyContactRelationship: String = "Spouse",
    val emergencyContactPhone: String = "",
    val emergencyContactSecondaryPhone: String = "",
    val status: PatientStatus = PatientStatus.ACTIVE,
    val errors: PatientFormErrors = PatientFormErrors(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class PatientEditViewModel(
    private val patientId: String,
    private val patientRepository: PatientRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<AuthUser?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(PatientEditState(patientId = patientId))
    val uiState: StateFlow<PatientEditState> = _uiState.asStateFlow()

    private val _saveSuccessEvent = MutableSharedFlow<Unit>()
    val saveSuccessEvent: SharedFlow<Unit> = _saveSuccessEvent.asSharedFlow()

    init {
        loadPatient()
    }

    private fun loadPatient() {
        viewModelScope.launch {
            patientRepository.getPatientById(patientId).collect { patient ->
                if (patient != null && !_uiState.value.initialLoaded) {
                    _uiState.update {
                        it.copy(
                            initialLoaded = true,
                            hospitalRegNo = patient.hospitalRegNo,
                            fullName = patient.fullName,
                            dob = patient.dob,
                            gender = patient.gender,
                            nationalIdOrPassport = patient.nationalIdOrPassport,
                            phone = patient.phone,
                            email = patient.email,
                            address = patient.address,
                            preferredLanguage = patient.preferredLanguage,
                            bloodGroup = patient.bloodGroup,
                            allergies = patient.allergies,
                            existingConditions = patient.existingConditions,
                            emergencyContactName = patient.emergencyContactName,
                            emergencyContactRelationship = patient.emergencyContactRelationship,
                            emergencyContactPhone = patient.emergencyContactPhone,
                            emergencyContactSecondaryPhone = patient.emergencyContactSecondaryPhone,
                            status = patient.status
                        )
                    }
                }
            }
        }
    }

    fun onPhoneChanged(v: String) = _uiState.update { it.copy(phone = v, errors = it.errors.copy(phone = null)) }
    fun onEmailChanged(v: String) = _uiState.update { it.copy(email = v, errors = it.errors.copy(email = null)) }
    fun onAddressChanged(v: String) = _uiState.update { it.copy(address = v) }
    fun onLanguageChanged(v: String) = _uiState.update { it.copy(preferredLanguage = v) }
    fun onEmergencyNameChanged(v: String) = _uiState.update { it.copy(emergencyContactName = v, errors = it.errors.copy(emergencyContactName = null)) }
    fun onEmergencyRelChanged(v: String) = _uiState.update { it.copy(emergencyContactRelationship = v) }
    fun onEmergencyPhoneChanged(v: String) = _uiState.update { it.copy(emergencyContactPhone = v, errors = it.errors.copy(emergencyContactPhone = null)) }
    fun onEmergencySecPhoneChanged(v: String) = _uiState.update { it.copy(emergencyContactSecondaryPhone = v) }
    fun onAllergiesChanged(v: String) = _uiState.update { it.copy(allergies = v) }
    fun onConditionsChanged(v: String) = _uiState.update { it.copy(existingConditions = v) }
    fun onStatusChanged(s: PatientStatus) = _uiState.update { it.copy(status = s) }

    fun canArchive(): Boolean = currentUser.value?.hasPermission(Permission.ARCHIVE_PATIENT) == true

    fun saveChanges() {
        val state = _uiState.value
        val validationErrors = PatientValidator.validate(
            fullName = state.fullName,
            dob = state.dob,
            phone = state.phone,
            email = state.email,
            emergencyContactName = state.emergencyContactName,
            emergencyContactPhone = state.emergencyContactPhone
        )

        if (validationErrors.hasErrors) {
            _uiState.update { it.copy(errors = validationErrors, errorMessage = "Please correct the highlighted validation errors") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val updatedPatient = Patient(
                id = state.patientId,
                hospitalRegNo = state.hospitalRegNo,
                fullName = state.fullName,
                dob = state.dob,
                gender = state.gender,
                nationalIdOrPassport = state.nationalIdOrPassport,
                phone = state.phone.trim(),
                email = state.email.trim(),
                address = state.address.trim(),
                preferredLanguage = state.preferredLanguage,
                bloodGroup = state.bloodGroup,
                allergies = state.allergies.trim(),
                existingConditions = state.existingConditions.trim(),
                emergencyContactName = state.emergencyContactName.trim(),
                emergencyContactRelationship = state.emergencyContactRelationship.trim(),
                emergencyContactPhone = state.emergencyContactPhone.trim(),
                emergencyContactSecondaryPhone = state.emergencyContactSecondaryPhone.trim(),
                registeredDate = System.currentTimeMillis(),
                status = state.status,
                createdAt = 0L,
                updatedAt = System.currentTimeMillis(),
                createdBy = "",
                updatedBy = ""
            )

            when (val res = patientRepository.updatePatientDemographics(updatedPatient)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _saveSuccessEvent.emit(Unit)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
                else -> Unit
            }
        }
    }

    companion object {
        fun provideFactory(
            patientId: String,
            patientRepository: PatientRepository,
            authRepository: AuthRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PatientEditViewModel(patientId, patientRepository, authRepository) as T
                }
            }
    }
}
