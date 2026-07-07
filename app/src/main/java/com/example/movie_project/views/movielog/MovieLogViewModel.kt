package com.example.movie_project.views.movielog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_project.data.local.MovieLogEntry
import com.example.movie_project.data.repository.AuthRepository
import com.example.movie_project.data.repository.MovieLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MovieLogUiState(
    val entries: List<MovieLogEntry> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class MovieLogViewModel @Inject constructor(
    private val repository: MovieLogRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovieLogUiState(isLoading = true))
    val uiState: StateFlow<MovieLogUiState> = _uiState.asStateFlow()

    init {
        val currentUserId = authRepository.currentUserId

        if (currentUserId != null) {
            viewModelScope.launch {
                repository.observeEntries(currentUserId).collect { list ->
                    _uiState.update { it.copy(entries = list, isLoading = false) }
                }
            }
            viewModelScope.launch {
                repository.errorMessage.collect { msg ->
                    if (msg.isNotEmpty()) _uiState.update { it.copy(errorMessage = msg) }
                }
            }
            repository.startFirebaseListener(currentUserId)
        } else {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Please sign in to view your movie log") }
        }
    }
}
