package com.example.movie_project.views.movielog

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_project.data.local.MovieLogEntry
import com.example.movie_project.data.repository.MovieLogRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MovieLogDetailUiState(
    val entry: MovieLogEntry? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface MovieLogDetailEvent {
    data object Saved : MovieLogDetailEvent
    data object Deleted : MovieLogDetailEvent
}

@HiltViewModel
class MovieLogDetailViewModel @Inject constructor(
    private val repository: MovieLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovieLogDetailUiState())
    val uiState: StateFlow<MovieLogDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MovieLogDetailEvent>()
    val events: SharedFlow<MovieLogDetailEvent> = _events.asSharedFlow()

    private val userId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    fun loadEntry(entryId: String) {
        val uid = userId ?: run {
            _uiState.update { it.copy(errorMessage = "Please sign in to view entries") }
            return
        }
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            repository.getEntryById(uid, entryId).collect { entry ->
                _uiState.update { it.copy(entry = entry, isLoading = false) }
            }
        }
    }

    fun insertEntry(entry: MovieLogEntry) {
        val uid = userId ?: run {
            _uiState.update { it.copy(errorMessage = "Please sign in to save entries") }
            return
        }
        viewModelScope.launch {
            try {
                repository.addEntry(uid, entry)
                _events.emit(MovieLogDetailEvent.Saved)
            } catch (e: Exception) {
                Log.e("MovieLogDetailVM", "Insert failed: ${e.message}")
                _uiState.update { it.copy(errorMessage = e.localizedMessage ?: "Failed to save movie log entry") }
            }
        }
    }

    fun updateEntry(entry: MovieLogEntry) {
        val uid = userId ?: run {
            _uiState.update { it.copy(errorMessage = "Please sign in to update entries") }
            return
        }
        viewModelScope.launch {
            try {
                repository.updateEntry(uid, entry)
                _events.emit(MovieLogDetailEvent.Saved)
            } catch (e: Exception) {
                Log.e("MovieLogDetailVM", "Update failed: ${e.message}")
                _uiState.update { it.copy(errorMessage = e.localizedMessage ?: "Failed to update movie log entry") }
            }
        }
    }

    fun deleteEntry(entryId: String) {
        val uid = userId ?: run {
            _uiState.update { it.copy(errorMessage = "Please sign in to delete entries") }
            return
        }
        viewModelScope.launch {
            try {
                repository.deleteEntry(uid, entryId)
                _events.emit(MovieLogDetailEvent.Deleted)
            } catch (e: Exception) {
                Log.e("MovieLogDetailVM", "Delete failed: ${e.message}")
                _uiState.update { it.copy(errorMessage = e.localizedMessage ?: "Failed to delete movie log entry") }
            }
        }
    }
}
