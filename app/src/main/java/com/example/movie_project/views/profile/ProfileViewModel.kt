package com.example.movie_project.views.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_project.data.repository.AuthRepository
import com.example.movie_project.data.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val userEmail: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(userEmail = authRepository.currentUserEmail) }
    }

    fun signOut() {
        val uid = authRepository.currentUserId
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                if (uid != null) {
                    favoritesRepository.pushPendingToFirebase(uid)
                    favoritesRepository.stopFirebaseListener()
                    favoritesRepository.clearLocalForUser(uid)
                }
            } finally {
                // authRepository.signOut() flips authState → null, which the nav
                // host observes to clear the back stack and route to Login.
                authRepository.signOut()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
