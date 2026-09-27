package com.example.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.model.AuthUser
import com.example.data.local.CareFlowDatabase
import com.example.data.local.entity.QueueTicketEntity
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardMetrics(
    val waitingQueueCount: Int = 0,
    val inConsultationCount: Int = 0,
    val totalPatients: Int = 0,
    val todayAppointments: Int = 0,
    val pendingPrescriptions: Int = 0,
    val pendingLabOrders: Int = 0,
    val pendingInvoices: Int = 0,
    val totalRevenue: Double = 0.0,
    val activeTickets: List<QueueTicketEntity> = emptyList()
)

private data class OperationalMetrics(
    val waiting: Int,
    val inConsult: Int,
    val patients: Int,
    val appointments: Int
)

private data class ClinicalAndFinancialMetrics(
    val rx: Int,
    val labs: Int,
    val invoices: Int,
    val revenue: Double,
    val tickets: List<QueueTicketEntity>
)

class RoleDashboardViewModel(
    private val authRepository: AuthRepository,
    private val database: CareFlowDatabase
) : ViewModel() {

    val currentUser: StateFlow<AuthUser?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    private val startOfDay = calendar.timeInMillis
    private val endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1

    private val operationalFlow = combine(
        database.queueDao().getWaitingCount(),
        database.queueDao().getInConsultationCount(),
        database.patientDao().getPatientCount(),
        database.appointmentDao().getTodayAppointmentCount(startOfDay, endOfDay)
    ) { waiting, inConsult, patients, appts ->
        OperationalMetrics(waiting, inConsult, patients, appts)
    }

    private val clinicalAndFinancialFlow = combine(
        database.prescriptionDao().getPendingCount(),
        database.labDao().getPendingCount(),
        database.billingDao().getPendingInvoiceCount(),
        database.billingDao().getTotalRevenue(),
        database.queueDao().getActiveQueue()
    ) { rx, labs, inv, rev, tickets ->
        ClinicalAndFinancialMetrics(rx, labs, inv, rev ?: 0.0, tickets)
    }

    val metrics: StateFlow<DashboardMetrics> = combine(
        operationalFlow,
        clinicalAndFinancialFlow
    ) { op, cf ->
        DashboardMetrics(
            waitingQueueCount = op.waiting,
            inConsultationCount = op.inConsult,
            totalPatients = op.patients,
            todayAppointments = op.appointments,
            pendingPrescriptions = cf.rx,
            pendingLabOrders = cf.labs,
            pendingInvoices = cf.invoices,
            totalRevenue = cf.revenue,
            activeTickets = cf.tickets
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }

    companion object {
        fun provideFactory(
            authRepository: AuthRepository,
            database: CareFlowDatabase
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RoleDashboardViewModel(authRepository, database) as T
                }
            }
    }
}
