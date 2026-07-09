package com.example.movie_project.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_project.data.repository.AuthRepository
import com.example.movie_project.data.repository.FavoritesRepository
import com.example.movie_project.models.domain.MovieModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val favorites: List<MovieModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: FavoritesRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState(isLoading = true))
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        // Re-observe whenever the signed-in user changes so switching accounts
        // never shows the previous user's favorites.
        viewModelScope.launch {
            authRepository.authState().collectLatest { uid ->
                if (uid == null) {
                    repository.stopFirebaseListener()
                    _uiState.update {
                        it.copy(
                            favorites = emptyList(),
                            isLoading = false,
                            errorMessage = "Please sign in to view favorites"
                        )
                    }
                    return@collectLatest
                }
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                repository.startFirebaseListener(uid)
                repository.observeFavorites(uid).collect { list ->
                    _uiState.update { it.copy(favorites = list, isLoading = false) }
                }
            }
        }
        viewModelScope.launch {
            repository.errorMessage.collect { msg ->
                if (msg.isNotEmpty()) _uiState.update { it.copy(errorMessage = msg) }
            }
        }
    }
}