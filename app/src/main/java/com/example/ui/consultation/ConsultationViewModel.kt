package com.example.ui.consultation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.model.Patient
import com.example.core.model.QueueTicket
import com.example.core.util.Resource
import com.example.data.local.entity.ClinicalEncounterEntity
import com.example.data.local.entity.VitalSignEntity
import com.example.data.repository.BillingRepository
import com.example.data.repository.ClinicalRepository
import com.example.data.repository.PatientRepository
import com.example.data.repository.QueueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrescribedDrug(
    val name: String,
    val dosage: String,
    val frequency: String,
    val duration: String,
    val instructions: String = "Take after meals"
)

data class ConsultationUiState(
    val ticket: QueueTicket? = null,
    val patient: Patient? = null,
    val latestVitals: VitalSignEntity? = null,
    val previousEncounters: List<ClinicalEncounterEntity> = emptyList(),
    val chiefComplaint: String = "",
    val historyOfPresentIllness: String = "",
    val examinationNotes: String = "",
    val assessment: String = "",
    val clinicianDiagnosis: String = "",
    val treatmentPlan: String = "",
    val followUpInstructions: String = "Return in 2 weeks for review or if symptoms worsen.",
    val prescribedDrugs: List<PrescribedDrug> = emptyList(),
    val selectedLabTests: List<String> = emptyList(),
    val labOrderPriority: String = "ROUTINE",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isCompleted: Boolean = false
)

class ConsultationViewModel(
    private val ticketId: String,
    private val queueRepository: QueueRepository,
    private val patientRepository: PatientRepository,
    private val clinicalRepository: ClinicalRepository,
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConsultationUiState(isLoading = true))
    val uiState: StateFlow<ConsultationUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val ticket = queueRepository.getTicketById(ticketId).firstOrNull()
            if (ticket != null) {
                val patient = patientRepository.getPatientById(ticket.patientId).firstOrNull()
                val vitals = clinicalRepository.getLatestVitals(ticket.patientId).firstOrNull()
                val encounters = clinicalRepository.getPatientEncounters(ticket.patientId).firstOrNull() ?: emptyList()

                _uiState.update {
                    it.copy(
                        ticket = ticket,
                        patient = patient,
                        latestVitals = vitals,
                        previousEncounters = encounters,
                        chiefComplaint = ticket.notes ?: "",
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Ticket not found: $ticketId") }
            }
        }
    }

    fun updateChiefComplaint(v: String) = _uiState.update { it.copy(chiefComplaint = v) }
    fun updateHistory(v: String) = _uiState.update { it.copy(historyOfPresentIllness = v) }
    fun updateExamination(v: String) = _uiState.update { it.copy(examinationNotes = v) }
    fun updateAssessment(v: String) = _uiState.update { it.copy(assessment = v) }
    fun updateDiagnosis(v: String) = _uiState.update { it.copy(clinicianDiagnosis = v) }
    fun updateTreatmentPlan(v: String) = _uiState.update { it.copy(treatmentPlan = v) }
    fun updateFollowUp(v: String) = _uiState.update { it.copy(followUpInstructions = v) }

    fun addPrescriptionDrug(drug: PrescribedDrug) {
        _uiState.update { it.copy(prescribedDrugs = it.prescribedDrugs + drug) }
    }

    fun removePrescriptionDrug(index: Int) {
        _uiState.update {
            val list = it.prescribedDrugs.toMutableList()
            if (index in list.indices) list.removeAt(index)
            it.copy(prescribedDrugs = list)
        }
    }

    fun toggleLabTest(testName: String) {
        _uiState.update { state ->
            val updated = if (state.selectedLabTests.contains(testName)) {
                state.selectedLabTests - testName
            } else {
                state.selectedLabTests + testName
            }
            state.copy(selectedLabTests = updated)
        }
    }

    fun setLabPriority(priority: String) = _uiState.update { it.copy(labOrderPriority = priority) }

    fun finalizeConsultation() {
        val state = _uiState.value
        val patientId = state.ticket?.patientId ?: return

        if (state.chiefComplaint.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter chief complaint") }
            return
        }
        if (state.clinicianDiagnosis.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please document clinician diagnosis") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. Record clinical encounter
            val encounterResult = clinicalRepository.recordEncounter(
                patientId = patientId,
                doctorId = state.ticket.doctorId,
                chiefComplaint = state.chiefComplaint,
                historyOfPresentIllness = state.historyOfPresentIllness,
                examinationNotes = state.examinationNotes,
                assessment = state.assessment,
                clinicianDiagnosis = state.clinicianDiagnosis,
                treatmentPlan = state.treatmentPlan,
                followUpInstructions = state.followUpInstructions
            )

            if (encounterResult is Resource.Error) {
                _uiState.update { it.copy(isLoading = false, errorMessage = encounterResult.message) }
                return@launch
            }

            val encounterId = (encounterResult as? Resource.Success)?.data?.id

            // 2. Issue prescription if drugs were prescribed
            var prescriptionTotal = 0.0
            if (state.prescribedDrugs.isNotEmpty()) {
                val itemsJson = buildDrugJson(state.prescribedDrugs)
                clinicalRepository.issuePrescription(
                    patientId = patientId,
                    doctorId = state.ticket.doctorId,
                    encounterId = encounterId,
                    itemsJson = itemsJson,
                    instructions = state.followUpInstructions
                )
                prescriptionTotal = state.prescribedDrugs.size * 15.0
            }

            // 3. Issue lab orders if tests selected
            var labTotal = 0.0
            for (testName in state.selectedLabTests) {
                clinicalRepository.orderLabTest(
                    patientId = patientId,
                    doctorId = state.ticket.doctorId,
                    testName = testName,
                    priority = state.labOrderPriority
                )
                labTotal += 35.0
            }

            // 4. Generate billing invoice for consultation + services
            val consultationFee = 50.0
            val grandTotal = consultationFee + prescriptionTotal + labTotal
            val invoiceItemsJson = """[{"item":"Physician Consultation","amount":$consultationFee},{"item":"Prescription Medicines (${state.prescribedDrugs.size})","amount":$prescriptionTotal},{"item":"Laboratory Investigations (${state.selectedLabTests.size})","amount":$labTotal}]"""
            billingRepository.createInvoice(
                patientId = patientId,
                totalAmount = grandTotal,
                itemsJson = invoiceItemsJson
            )

            // 5. Complete queue ticket
            queueRepository.completeConsultation(state.ticket.id)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isCompleted = true,
                    successMessage = "Consultation finalized. Prescription sent to pharmacy, lab orders dispatched, and invoice generated."
                )
            }
        }
    }

    private fun buildDrugJson(drugs: List<PrescribedDrug>): String {
        val items = drugs.joinToString(",") { drug ->
            """{"drug":"${drug.name}","dosage":"${drug.dosage}","frequency":"${drug.frequency}","duration":"${drug.duration}","instructions":"${drug.instructions}"}"""
        }
        return "[$items]"
    }

    fun clearMessages() = _uiState.update { it.copy(errorMessage = null, successMessage = null) }

    companion object {
        fun provideFactory(
            ticketId: String,
            queueRepository: QueueRepository,
            patientRepository: PatientRepository,
            clinicalRepository: ClinicalRepository,
            billingRepository: BillingRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ConsultationViewModel(
                    ticketId,
                    queueRepository,
                    patientRepository,
                    clinicalRepository,
                    billingRepository
                ) as T
            }
        }
    }
}
