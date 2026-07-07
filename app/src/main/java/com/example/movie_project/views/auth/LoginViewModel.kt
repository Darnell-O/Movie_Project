package com.example.movie_project.views.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val navigateToMain: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email) }
    fun onPasswordChange(password: String) = _uiState.update { it.copy(password = password) }

    fun login() {
        val state = _uiState.value
        if (state.email.isEmpty() || state.password.isEmpty()) {
            _uiState.update { it.copy(error = "Email and password are required") }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        auth.signInWithEmailAndPassword(state.email, state.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, navigateToMain = true) }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = task.exception?.localizedMessage ?: "Login failed. Please try again.")
                    }
                }
            }
    }

    fun onNavigatedToMain() = _uiState.update { it.copy(navigateToMain = false) }

    fun isAlreadySignedIn(): Boolean = auth.currentUser != null
}
