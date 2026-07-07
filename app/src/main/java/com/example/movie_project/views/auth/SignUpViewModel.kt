package com.example.movie_project.views.auth

import androidx.lifecycle.ViewModel
import com.example.movie_project.models.domain.UsersModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
class SignUpViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

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
        auth.createUserWithEmailAndPassword(state.email, state.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.uid ?: ""
                    FirebaseDatabase.getInstance().getReference("users")
                        .child("user").child(uid).setValue(UsersModel(state.email, uid))
                    _uiState.update { it.copy(isLoading = false, navigateToMain = true) }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = task.exception?.localizedMessage ?: "Sign up failed. Please try again.")
                    }
                }
            }
    }

    fun signInWithGoogle(idToken: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, navigateToMain = true) }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = task.exception?.localizedMessage ?: "Google sign-in failed. Please try again.")
                    }
                }
            }
    }

    fun onNavigatedToMain() = _uiState.update { it.copy(navigateToMain = false) }
}
