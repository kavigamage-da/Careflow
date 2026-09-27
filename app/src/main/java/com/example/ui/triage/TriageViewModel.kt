package com.example.ui.triage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.model.Patient
import com.example.core.model.QueuePriority
import com.example.core.model.QueueTicket
import com.example.core.util.Resource
import com.example.data.local.entity.VitalSignEntity
import com.example.data.repository.ClinicalRepository
import com.example.data.repository.PatientRepository
import com.example.data.repository.QueueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TriageUiState(
    val ticket: QueueTicket? = null,
    val patient: Patient? = null,
    val previousVitals: List<VitalSignEntity> = emptyList(),
    val temperatureCelsius: String = "36.8",
    val systolicBp: String = "120",
    val diastolicBp: String = "80",
    val pulseRate: String = "72",
    val respiratoryRate: String = "16",
    val oxygenSaturation: String = "98",
    val weightKg: String = "70.0",
    val heightCm: String = "172.0",
    val painScore: Int = 0,
    val urgentAttentionFlag: Boolean = false,
    val triageNotes: String = "",
    val triagePriority: QueuePriority = QueuePriority.STANDARD,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isSaved: Boolean = false
) {
    val calculatedBmi: Float?
        get() {
            val weight = weightKg.toFloatOrNull() ?: return null
            val height = heightCm.toFloatOrNull() ?: return null
            if (height <= 0f) return null
            val heightM = height / 100f
            return weight / (heightM * heightM)
        }
}

class TriageViewModel(
    private val ticketId: String,
    private val queueRepository: QueueRepository,
    private val patientRepository: PatientRepository,
    private val clinicalRepository: ClinicalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TriageUiState(isLoading = true))
    val uiState: StateFlow<TriageUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val ticket = queueRepository.getTicketById(ticketId).firstOrNull()
            if (ticket != null) {
                val patient = patientRepository.getPatientById(ticket.patientId).firstOrNull()
                clinicalRepository.getPatientVitals(ticket.patientId).collect { vitalsList ->
                    _uiState.update {
                        it.copy(
                            ticket = ticket,
                            patient = patient,
                            previousVitals = vitalsList,
                            triagePriority = ticket.priority,
                            isLoading = false
                        )
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Queue ticket not found: $ticketId") }
            }
        }
    }

    fun updateTemperature(value: String) = _uiState.update { it.copy(temperatureCelsius = value) }
    fun updateSystolicBp(value: String) = _uiState.update { it.copy(systolicBp = value) }
    fun updateDiastolicBp(value: String) = _uiState.update { it.copy(diastolicBp = value) }
    fun updatePulseRate(value: String) = _uiState.update { it.copy(pulseRate = value) }
    fun updateRespiratoryRate(value: String) = _uiState.update { it.copy(respiratoryRate = value) }
    fun updateOxygenSaturation(value: String) = _uiState.update { it.copy(oxygenSaturation = value) }
    fun updateWeight(value: String) = _uiState.update { it.copy(weightKg = value) }
    fun updateHeight(value: String) = _uiState.update { it.copy(heightCm = value) }
    fun updatePainScore(score: Int) = _uiState.update { it.copy(painScore = score) }
    fun updateUrgentFlag(flag: Boolean) = _uiState.update {
        it.copy(
            urgentAttentionFlag = flag,
            triagePriority = if (flag && it.triagePriority == QueuePriority.STANDARD) QueuePriority.URGENT else it.triagePriority
        )
    }
    fun updateTriageNotes(notes: String) = _uiState.update { it.copy(triageNotes = notes) }
    fun updateTriagePriority(priority: QueuePriority) = _uiState.update { it.copy(triagePriority = priority) }

    fun saveTriage() {
        val state = _uiState.value
        val patientId = state.ticket?.patientId ?: return

        val temp = state.temperatureCelsius.toFloatOrNull()
        val sys = state.systolicBp.toIntOrNull()
        val dia = state.diastolicBp.toIntOrNull()
        val pulse = state.pulseRate.toIntOrNull()
        val resp = state.respiratoryRate.toIntOrNull()
        val spo2 = state.oxygenSaturation.toIntOrNull()
        val weight = state.weightKg.toFloatOrNull()
        val height = state.heightCm.toFloatOrNull()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = clinicalRepository.recordVitals(
                patientId = patientId,
                temperatureCelsius = temp,
                systolicBp = sys,
                diastolicBp = dia,
                pulseRateBpm = pulse,
                respiratoryRateBpm = resp,
                oxygenSaturationPercent = spo2,
                weightKg = weight,
                heightCm = height,
                painScore = state.painScore,
                urgentAttentionFlag = state.urgentAttentionFlag,
                triageNotes = state.triageNotes
            )

            when (result) {
                is Resource.Success -> {
                    // Update priority if changed
                    if (state.ticket.priority != state.triagePriority) {
                        queueRepository.updatePriority(
                            ticketId = state.ticket.id,
                            newPriority = state.triagePriority,
                            reason = "Nurse triage evaluation (${state.triageNotes})"
                        )
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSaved = true,
                            successMessage = "Triage vitals recorded successfully. Patient ready for consultation."
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

    fun clearMessages() = _uiState.update { it.copy(errorMessage = null, successMessage = null) }

    companion object {
        fun provideFactory(
            ticketId: String,
            queueRepository: QueueRepository,
            patientRepository: PatientRepository,
            clinicalRepository: ClinicalRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TriageViewModel(ticketId, queueRepository, patientRepository, clinicalRepository) as T
            }
        }
    }
}
