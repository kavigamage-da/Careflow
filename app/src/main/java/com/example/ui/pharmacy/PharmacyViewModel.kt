package com.example.ui.pharmacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.util.Resource
import com.example.data.local.entity.PrescriptionEntity
import com.example.data.repository.PatientRepository
import com.example.data.repository.PharmacyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PharmacyUiState(
    val pendingPrescriptions: List<PrescriptionEntity> = emptyList(),
    val allPrescriptions: List<PrescriptionEntity> = emptyList(),
    val selectedPrescription: PrescriptionEntity? = null,
    val selectedTab: Int = 0, // 0 = Pending, 1 = Dispensed History
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class PharmacyViewModel(
    private val pharmacyRepository: PharmacyRepository,
    private val patientRepository: PatientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PharmacyUiState(isLoading = true))
    val uiState: StateFlow<PharmacyUiState> = _uiState.asStateFlow()

    init {
        loadPrescriptions()
    }

    private fun loadPrescriptions() {
        viewModelScope.launch {
            pharmacyRepository.getAllPrescriptions().collect { all ->
                val pending = all.filter { it.status == "ISSUED" || it.status == "DISPENSING" }
                _uiState.update {
                    it.copy(
                        allPrescriptions = all,
                        pendingPrescriptions = pending,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setSelectedTab(tab: Int) = _uiState.update { it.copy(selectedTab = tab) }
    fun selectPrescription(prescription: PrescriptionEntity?) = _uiState.update { it.copy(selectedPrescription = prescription) }

    fun dispensePrescription(prescriptionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = pharmacyRepository.dispensePrescription(prescriptionId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            selectedPrescription = null,
                            successMessage = "Prescription dispensed successfully."
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
            pharmacyRepository: PharmacyRepository,
            patientRepository: PatientRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PharmacyViewModel(pharmacyRepository, patientRepository) as T
            }
        }
    }
}
