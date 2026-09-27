package com.example.ui.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.util.Resource
import com.example.data.local.entity.InvoiceEntity
import com.example.data.repository.BillingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BillingUiState(
    val pendingInvoices: List<InvoiceEntity> = emptyList(),
    val allInvoices: List<InvoiceEntity> = emptyList(),
    val totalRevenue: Double = 0.0,
    val selectedTab: Int = 0, // 0 = Pending Invoices, 1 = Paid Receipts
    val invoiceForPayment: InvoiceEntity? = null,
    val receiptToView: InvoiceEntity? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class BillingViewModel(
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillingUiState(isLoading = true))
    val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()

    init {
        loadBillingData()
    }

    private fun loadBillingData() {
        viewModelScope.launch {
            billingRepository.getAllInvoices().collect { all ->
                val pending = all.filter { it.status == "PENDING" || it.status == "PARTIALLY_PAID" }
                _uiState.update {
                    it.copy(
                        allInvoices = all,
                        pendingInvoices = pending,
                        isLoading = false
                    )
                }
            }
        }

        viewModelScope.launch {
            billingRepository.getTotalRevenue().collect { rev ->
                _uiState.update { it.copy(totalRevenue = rev ?: 0.0) }
            }
        }
    }

    fun setSelectedTab(tab: Int) = _uiState.update { it.copy(selectedTab = tab) }
    fun selectInvoiceForPayment(invoice: InvoiceEntity?) = _uiState.update { it.copy(invoiceForPayment = invoice) }
    fun viewReceipt(invoice: InvoiceEntity?) = _uiState.update { it.copy(receiptToView = invoice) }

    fun processPayment(invoiceId: String, amount: Double, method: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = billingRepository.processPayment(invoiceId, amount, method)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            invoiceForPayment = null,
                            receiptToView = result.data,
                            successMessage = "Payment of $${String.format("%.2f", amount)} processed successfully."
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
            billingRepository: BillingRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BillingViewModel(billingRepository) as T
            }
        }
    }
}
