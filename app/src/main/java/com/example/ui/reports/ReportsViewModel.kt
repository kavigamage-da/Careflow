package com.example.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CareFlowDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OperationalReportData(
    val totalPatients: Int = 0,
    val totalConsultations: Int = 0,
    val totalQueueTickets: Int = 0,
    val waitingTickets: Int = 0,
    val totalPrescriptions: Int = 0,
    val dispensedPrescriptions: Int = 0,
    val totalLabOrders: Int = 0,
    val verifiedLabOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val pendingRevenue: Double = 0.0,
    val totalAuditLogs: Int = 0,
    val isLoading: Boolean = false
)

class ReportsViewModel(
    private val database: CareFlowDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OperationalReportData(isLoading = true))
    val uiState: StateFlow<OperationalReportData> = _uiState.asStateFlow()

    init {
        loadReportData()
    }

    private fun loadReportData() {
        viewModelScope.launch {
            val patientFlow = database.patientDao().getAllPatients()
            val consultFlow = database.clinicalDao().getAllEncounters()
            val queueFlow = database.queueDao().getActiveQueue()
            val presFlow = database.prescriptionDao().getAllPrescriptions()
            val labFlow = database.labDao().getAllLabOrders()
            val invoiceFlow = database.billingDao().getAllInvoices()

            val clinicalGroup = combine(patientFlow, consultFlow, queueFlow) { p, c, q ->
                Triple(p, c, q)
            }
            val operationsGroup = combine(presFlow, labFlow, invoiceFlow) { pr, l, inv ->
                Triple(pr, l, inv)
            }

            combine(clinicalGroup, operationsGroup) { (p, c, q), (pr, l, inv) ->
                val dispensedCount = pr.count { it.status == "DISPENSED" }
                val verifiedLabCount = l.count { it.status == "VERIFIED" }
                val paidTotal = inv.filter { it.status == "PAID" }.sumOf { it.paidAmount }
                val pendingTotal = inv.filter { it.status == "PENDING" || it.status == "PARTIALLY_PAID" }.sumOf { it.totalAmount - it.paidAmount }

                OperationalReportData(
                    totalPatients = p.size,
                    totalConsultations = c.size,
                    totalQueueTickets = q.size,
                    waitingTickets = q.count { it.status == "WAITING" },
                    totalPrescriptions = pr.size,
                    dispensedPrescriptions = dispensedCount,
                    totalLabOrders = l.size,
                    verifiedLabOrders = verifiedLabCount,
                    totalRevenue = paidTotal,
                    pendingRevenue = pendingTotal,
                    isLoading = false
                )
            }.collect { report ->
                _uiState.value = report
            }
        }
    }

    companion object {
        fun provideFactory(database: CareFlowDatabase): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ReportsViewModel(database) as T
                }
            }
    }
}
