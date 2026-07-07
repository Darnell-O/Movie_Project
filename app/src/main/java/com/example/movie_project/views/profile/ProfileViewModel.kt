package com.example.movie_project.views.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_project.data.repository.FavoritesRepository
import com.google.firebase.auth.FirebaseAuth
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
    val error: String? = null,
    val navigateToLogin: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(userEmail = FirebaseAuth.getInstance().currentUser?.email) }
    }

    fun signOut() {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                if (uid != null) {
                    favoritesRepository.pushPendingToFirebase(uid)
                    favoritesRepository.stopFirebaseListener()
                    favoritesRepository.clearLocalForUser(uid)
                }
            } finally {
                auth.signOut()
                _uiState.update { it.copy(isLoading = false, navigateToLogin = true) }
            }
        }
    }

    fun onNavigatedToLogin() = _uiState.update { it.copy(navigateToLogin = false) }
}
