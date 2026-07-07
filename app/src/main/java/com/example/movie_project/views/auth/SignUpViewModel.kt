package com.example.movie_project.views.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_project.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val navigateToMain: Boolean = false
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email) }
    fun onPasswordChange(password: String) = _uiState.update { it.copy(password = password) }
    fun onConfirmPasswordChange(cp: String) = _uiState.update { it.copy(confirmPassword = cp) }

    fun signUp() {
        val state = _uiState.value
        if (state.email.isEmpty() || state.password.isEmpty() || state.confirmPassword.isEmpty()) {
            _uiState.update { it.copy(error = "All fields are required") }
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(error = "Passwords do not match") }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            authRepository.signUp(state.email, state.password)
                .onSuccess { _uiState.update { it.copy(isLoading = false, navigateToMain = true) } }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.localizedMessage ?: "Sign up failed. Please try again.")
                    }
                }
        }
    }

    fun signInWithGoogle(idToken: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            authRepository.signInWithGoogle(idToken)
                .onSuccess { _uiState.update { it.copy(isLoading = false, navigateToMain = true) } }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.localizedMessage ?: "Google sign-in failed. Please try again.")
                    }
                }
        }
    }

    fun onNavigatedToMain() = _uiState.update { it.copy(navigateToMain = false) }
}