package com.example.movie_project.views.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.movie_project.data.repository.FavoritesRepository
import com.example.movie_project.models.domain.MovieModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val repository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState(isLoading = true))
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId != null) {
            viewModelScope.launch {
                repository.observeFavorites(currentUserId).collect { list ->
                    _uiState.update { it.copy(favorites = list, isLoading = false) }
                }
            }
            viewModelScope.launch {
                repository.errorMessage.asFlow().collect { msg ->
                    if (!msg.isNullOrEmpty()) _uiState.update { it.copy(errorMessage = msg) }
                }
            }
            repository.startFirebaseListener(currentUserId)
        } else {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Please sign in to view favorites") }
        }
    }
}
