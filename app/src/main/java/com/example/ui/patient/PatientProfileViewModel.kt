package com.example.ui.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.model.AuthUser
import com.example.core.model.Patient
import com.example.core.model.PatientStatus
import com.example.core.model.PatientTimelineEvent
import com.example.core.model.Permission
import com.example.core.util.Resource
import com.example.data.local.CareFlowDatabase
import com.example.data.local.entity.DepartmentEntity
import com.example.data.local.entity.DoctorEntity
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
import kotlinx.coroutines.launch

sealed class PatientProfileEvent {
    data class ShowToast(val message: String) : PatientProfileEvent()
    data class TicketGenerated(val ticketNumber: String) : PatientProfileEvent()
}

class PatientProfileViewModel(
    private val patientId: String,
    private val patientRepository: PatientRepository,
    private val authRepository: AuthRepository,
    private val database: CareFlowDatabase
) : ViewModel() {

    val currentUser: StateFlow<AuthUser?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val patient: StateFlow<Patient?> = patientRepository.getPatientById(patientId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val timelineEvents: StateFlow<List<PatientTimelineEvent>> = patientRepository.getPatientTimeline(patientId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val departments: StateFlow<List<DepartmentEntity>> = database.departmentDao().getActiveDepartments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val doctors: StateFlow<List<DoctorEntity>> = database.doctorDao().getAllDoctors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _events = MutableSharedFlow<PatientProfileEvent>()
    val events: SharedFlow<PatientProfileEvent> = _events.asSharedFlow()

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun checkInPatient(departmentId: String, doctorId: String?, priority: Int) {
        viewModelScope.launch {
            when (val res = patientRepository.checkInPatientToQueue(patientId, departmentId, doctorId, priority)) {
                is Resource.Success -> {
                    _events.emit(PatientProfileEvent.TicketGenerated(res.data))
                }
                is Resource.Error -> {
                    _events.emit(PatientProfileEvent.ShowToast(res.message))
                }
                else -> Unit
            }
        }
    }

    fun archivePatient(reason: String) {
        viewModelScope.launch {
            when (val res = patientRepository.updatePatientStatus(patientId, PatientStatus.ARCHIVED, reason)) {
                is Resource.Success -> {
                    _events.emit(PatientProfileEvent.ShowToast("Patient successfully archived"))
                }
                is Resource.Error -> {
                    _events.emit(PatientProfileEvent.ShowToast(res.message))
                }
                else -> Unit
            }
        }
    }

    fun canEditDemographics(): Boolean =
        currentUser.value?.hasPermission(Permission.EDIT_PATIENT) == true

    fun canManageQueue(): Boolean =
        currentUser.value?.hasPermission(Permission.MANAGE_QUEUE) == true

    fun canViewClinicalTimeline(): Boolean =
        currentUser.value?.hasPermission(Permission.VIEW_PATIENT_CLINICAL_TIMELINE) == true

    fun canArchivePatient(): Boolean =
        currentUser.value?.hasPermission(Permission.ARCHIVE_PATIENT) == true

    companion object {
        fun provideFactory(
            patientId: String,
            patientRepository: PatientRepository,
            authRepository: AuthRepository,
            database: CareFlowDatabase
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PatientProfileViewModel(patientId, patientRepository, authRepository, database) as T
                }
            }
    }
}
