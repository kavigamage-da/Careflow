package com.example.ui.lab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.util.Resource
import com.example.data.local.entity.LabOrderEntity
import com.example.data.repository.LabRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LabUiState(
    val pendingOrders: List<LabOrderEntity> = emptyList(),
    val allOrders: List<LabOrderEntity> = emptyList(),
    val selectedTab: Int = 0, // 0 = Pending, 1 = Verified / Completed
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class LabViewModel(
    private val labRepository: LabRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LabUiState(isLoading = true))
    val uiState: StateFlow<LabUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    private fun loadOrders() {
        viewModelScope.launch {
            labRepository.getAllLabOrders().collect { all ->
                val pending = all.filter { it.status in setOf("REQUESTED", "SAMPLE_COLLECTED", "PROCESSING") }
                _uiState.update {
                    it.copy(
                        allOrders = all,
                        pendingOrders = pending,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setSelectedTab(tab: Int) = _uiState.update { it.copy(selectedTab = tab) }

    fun collectSample(orderId: String, sampleType: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = labRepository.collectSample(orderId, sampleType)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, successMessage = "Sample collected for ${result.data?.testName}")
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun enterResult(orderId: String, resultText: String, notes: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = labRepository.enterResult(orderId, resultText, notes)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, successMessage = "Result recorded for ${result.data?.testName}")
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun verifyResult(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = labRepository.verifyResult(orderId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, successMessage = "Result verified for ${result.data?.testName}")
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
            labRepository: LabRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LabViewModel(labRepository) as T
            }
        }
    }
}
