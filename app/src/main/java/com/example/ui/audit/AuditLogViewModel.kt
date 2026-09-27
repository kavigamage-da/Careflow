package com.example.ui.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.model.AuditLog
import com.example.data.repository.AuditRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AuditLogViewModel(
    private val auditRepository: AuditRepository
) : ViewModel() {

    val logs: StateFlow<List<AuditLog>> = auditRepository.getRecentLogs(limit = 100)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logCount: StateFlow<Int> = auditRepository.getLogCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    companion object {
        fun provideFactory(auditRepository: AuditRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuditLogViewModel(auditRepository) as T
                }
            }
    }
}
