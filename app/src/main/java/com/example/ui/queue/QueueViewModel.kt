package com.example.ui.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.model.DepartmentQueueMetrics
import com.example.core.model.Patient
import com.example.core.model.QueuePriority
import com.example.core.model.QueueStatus
import com.example.core.model.QueueTicket
import com.example.core.model.UserRole
import com.example.core.security.SessionManager
import com.example.core.util.Resource
import com.example.data.repository.PatientRepository
import com.example.data.repository.QueueRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QueueUiState(
    val tickets: List<QueueTicket> = emptyList(),
    val filteredTickets: List<QueueTicket> = emptyList(),
    val selectedDepartment: String = "ALL",
    val selectedStatusFilter: String = "ACTIVE", // "ACTIVE", "WAITING", "CALLED", "ALL"
    val metrics: DepartmentQueueMetrics? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isCheckInDialogOpen: Boolean = false,
    val checkInPatientQuery: String = "",
    val checkInMatchingPatients: List<Patient> = emptyList(),
    val selectedPatientForCheckIn: Patient? = null,
    val checkInDepartmentId: String = "dept_gen_med",
    val checkInPriority: QueuePriority = QueuePriority.STANDARD,
    val checkInNotes: String = "",
    val currentUserRole: UserRole? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class QueueViewModel(
    private val queueRepository: QueueRepository,
    private val patientRepository: PatientRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _selectedDepartment = MutableStateFlow("ALL")
    private val _selectedStatusFilter = MutableStateFlow("ACTIVE")
    private val _uiState = MutableStateFlow(QueueUiState())
    val uiState: StateFlow<QueueUiState> = _uiState.asStateFlow()

    init {
        val user = sessionManager.getCurrentUser()
        _uiState.update { it.copy(currentUserRole = user?.role) }

        // Observe tickets reactively based on selected department
        viewModelScope.launch {
            _selectedDepartment.flatMapLatest { dept ->
                queueRepository.getActiveQueue(if (dept == "ALL") null else dept)
            }.collect { ticketList ->
                _uiState.update { state ->
                    val filtered = applyFilter(ticketList, state.selectedStatusFilter)
                    state.copy(tickets = ticketList, filteredTickets = filtered, isLoading = false)
                }
            }
        }

        // Observe department metrics if a specific department is selected
        viewModelScope.launch {
            _selectedDepartment.flatMapLatest { dept ->
                val deptId = if (dept == "ALL") "dept_gen_med" else dept
                queueRepository.getDepartmentMetrics(deptId)
            }.collect { metrics ->
                _uiState.update { it.copy(metrics = metrics) }
            }
        }
    }

    fun setDepartment(departmentId: String) {
        _selectedDepartment.value = departmentId
        _uiState.update { state ->
            val filtered = applyFilter(state.tickets, state.selectedStatusFilter)
            state.copy(selectedDepartment = departmentId, filteredTickets = filtered)
        }
    }

    fun setStatusFilter(filter: String) {
        _selectedStatusFilter.value = filter
        _uiState.update { state ->
            val filtered = applyFilter(state.tickets, filter)
            state.copy(selectedStatusFilter = filter, filteredTickets = filtered)
        }
    }

    private fun applyFilter(tickets: List<QueueTicket>, filter: String): List<QueueTicket> {
        return when (filter) {
            "WAITING" -> tickets.filter { it.status == QueueStatus.WAITING }
            "CALLED" -> tickets.filter { it.status == QueueStatus.CALLED }
            "IN_CONSULTATION" -> tickets.filter { it.status == QueueStatus.IN_CONSULTATION }
            "ACTIVE" -> tickets.filter { it.status in setOf(QueueStatus.WAITING, QueueStatus.CALLED, QueueStatus.IN_CONSULTATION) }
            else -> tickets
        }
    }

    fun callNext() {
        val dept = if (_selectedDepartment.value == "ALL") "dept_gen_med" else _selectedDepartment.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = queueRepository.callNextPatient(dept)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Called ticket ${result.data?.ticketNumber} (${result.data?.patientName})"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun recall(ticketId: String) {
        viewModelScope.launch {
            when (val result = queueRepository.recallPatient(ticketId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(successMessage = "Recalled ticket ${result.data?.ticketNumber}")
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun startConsultation(ticketId: String) {
        viewModelScope.launch {
            when (val result = queueRepository.startConsultation(ticketId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(successMessage = "Consultation started for ${result.data?.ticketNumber}")
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun completeConsultation(ticketId: String) {
        viewModelScope.launch {
            when (val result = queueRepository.completeConsultation(ticketId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(successMessage = "Completed ticket ${result.data?.ticketNumber}")
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun skipTicket(ticketId: String, reason: String = "No show after repeated calls") {
        viewModelScope.launch {
            when (val result = queueRepository.skipPatient(ticketId, reason)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(successMessage = "Marked ${result.data?.ticketNumber} as skipped")
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun updatePriority(ticketId: String, newPriority: QueuePriority, reason: String = "Triage evaluation") {
        viewModelScope.launch {
            when (val result = queueRepository.updatePriority(ticketId, newPriority, reason)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(successMessage = "Ticket ${result.data?.ticketNumber} escalated to ${newPriority.displayName}")
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun transferDepartment(ticketId: String, targetDepartmentId: String, reason: String) {
        viewModelScope.launch {
            when (val result = queueRepository.transferDepartment(ticketId, targetDepartmentId, reason)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(successMessage = "Ticket ${result.data?.ticketNumber} transferred")
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    // Check-in dialog handling
    fun openCheckInDialog(preselectedPatient: Patient? = null) {
        _uiState.update {
            it.copy(
                isCheckInDialogOpen = true,
                selectedPatientForCheckIn = preselectedPatient,
                checkInPatientQuery = preselectedPatient?.fullName ?: "",
                checkInPriority = QueuePriority.STANDARD,
                checkInNotes = "",
                errorMessage = null
            )
        }
    }

    fun closeCheckInDialog() {
        _uiState.update {
            it.copy(
                isCheckInDialogOpen = false,
                selectedPatientForCheckIn = null,
                checkInMatchingPatients = emptyList(),
                checkInPatientQuery = ""
            )
        }
    }

    fun searchPatientForCheckIn(query: String) {
        _uiState.update { it.copy(checkInPatientQuery = query) }
        if (query.length >= 2) {
            viewModelScope.launch {
                patientRepository.searchPatients(query).collect { list ->
                    _uiState.update { it.copy(checkInMatchingPatients = list) }
                }
            }
        } else {
            _uiState.update { it.copy(checkInMatchingPatients = emptyList()) }
        }
    }

    fun selectPatientForCheckIn(patient: Patient) {
        _uiState.update {
            it.copy(
                selectedPatientForCheckIn = patient,
                checkInPatientQuery = "${patient.fullName} (${patient.hospitalRegNo})",
                checkInMatchingPatients = emptyList()
            )
        }
    }

    fun setCheckInDepartment(deptId: String) {
        _uiState.update { it.copy(checkInDepartmentId = deptId) }
    }

    fun setCheckInPriority(priority: QueuePriority) {
        _uiState.update { it.copy(checkInPriority = priority) }
    }

    fun setCheckInNotes(notes: String) {
        _uiState.update { it.copy(checkInNotes = notes) }
    }

    fun submitCheckIn() {
        val patient = _uiState.value.selectedPatientForCheckIn
        if (patient == null) {
            _uiState.update { it.copy(errorMessage = "Please select a patient to check in") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = queueRepository.createTicket(
                patientId = patient.id,
                departmentId = _uiState.value.checkInDepartmentId,
                priority = _uiState.value.checkInPriority,
                notes = _uiState.value.checkInNotes.ifBlank { null }
            )

            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isCheckInDialogOpen = false,
                            selectedPatientForCheckIn = null,
                            successMessage = "Ticket ${result.data?.ticketNumber} issued for ${patient.fullName}"
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    companion object {
        fun provideFactory(
            queueRepository: QueueRepository,
            patientRepository: PatientRepository,
            sessionManager: SessionManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QueueViewModel(queueRepository, patientRepository, sessionManager) as T
            }
        }
    }
}
