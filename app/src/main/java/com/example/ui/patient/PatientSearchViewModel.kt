package com.example.ui.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.model.AuthUser
import com.example.core.model.Patient
import com.example.core.model.Permission
import com.example.data.repository.AuthRepository
import com.example.data.repository.PatientRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class PatientSearchViewModel(
    private val patientRepository: PatientRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<AuthUser?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("ALL")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _sortBy = MutableStateFlow("DATE_DESC")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val patients: StateFlow<List<Patient>> = combine(_searchQuery, _statusFilter, _sortBy) { query, status, sort ->
        Triple(query, status, sort)
    }.flatMapLatest { (query, status, sort) ->
        if (query.isBlank()) {
            patientRepository.getAllPatients(statusFilter = status, sortBy = sort)
        } else {
            patientRepository.searchPatients(query = query, statusFilter = status)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onStatusFilterChanged(status: String) {
        _statusFilter.value = status
    }

    fun onSortOrderChanged(sortBy: String) {
        _sortBy.value = sortBy
    }

    val canRegisterPatient: Boolean
        get() = currentUser.value?.hasPermission(Permission.REGISTER_PATIENT) == true

    companion object {
        fun provideFactory(
            patientRepository: PatientRepository,
            authRepository: AuthRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PatientSearchViewModel(patientRepository, authRepository) as T
                }
            }
    }
}
