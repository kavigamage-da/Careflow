package com.example.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.model.AuthUser
import com.example.core.model.UserRole
import com.example.core.util.Resource
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "admin",
    val password: String = "Password123!",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

sealed class LoginEvent {
    data class NavigateToDashboard(val user: AuthUser) : LoginEvent()
    object NavigateToChangePassword : LoginEvent()
}

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    fun onUsernameChange(username: String) {
        _uiState.update { it.copy(username = username, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun login() {
        val currentState = _uiState.value
        if (currentState.username.isBlank() || currentState.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter both username and password") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.login(currentState.username, currentState.password)
            _uiState.update { it.copy(isLoading = false) }

            when (result) {
                is Resource.Success -> {
                    val user = result.data
                    if (user.forcePasswordChange) {
                        _events.emit(LoginEvent.NavigateToChangePassword)
                    } else {
                        _events.emit(LoginEvent.NavigateToDashboard(user))
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                else -> Unit
            }
        }
    }

    fun selectDemoRole(role: UserRole) {
        val username = when (role) {
            UserRole.HOSPITAL_ADMIN -> "admin"
            UserRole.DOCTOR -> "doctor.carter"
            UserRole.NURSE -> "nurse.clara"
            UserRole.RECEPTIONIST -> "reception.ann"
            UserRole.QUEUE_OPERATOR -> "queue.operator"
            UserRole.LAB_TECHNICIAN -> "lab.tech"
            UserRole.PHARMACIST -> "pharma.alex"
            UserRole.CASHIER -> "cashier.david"
            UserRole.PATIENT -> "patient.john"
            UserRole.SUPER_ADMIN -> "super.admin"
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, username = username, password = "Password123!") }
            val result = authRepository.quickLoginRole(username)
            _uiState.update { it.copy(isLoading = false) }

            when (result) {
                is Resource.Success -> {
                    _events.emit(LoginEvent.NavigateToDashboard(result.data))
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
                else -> Unit
            }
        }
    }

    companion object {
        fun provideFactory(authRepository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LoginViewModel(authRepository) as T
                }
            }
    }
}
