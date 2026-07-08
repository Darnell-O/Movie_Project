package com.example.movie_project.views.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.movie_project.data.repository.AuthRepository
import com.example.movie_project.data.repository.FavoritesRepository
import com.example.movie_project.data.repository.MovieRepository
import com.example.movie_project.models.domain.MovieModel
import com.example.movie_project.views.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val movie: MovieModel? = null,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val favoritesRepository: FavoritesRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val movieId: Int = savedStateHandle.toRoute<Route.Detail>().movieId

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            movieRepository.getMovieDetails(movieId)
                .onSuccess { movie -> onMovieLoaded(movie) }
                .onFailure {
                    // Offline / failure fallback: use the cached favorite if we have one.
                    val cached = authRepository.currentUserId?.let { uid ->
                        favoritesRepository.getFavorite(uid, movieId)
                    }
                    if (cached != null) {
                        onMovieLoaded(cached)
                    } else {
                        _uiState.update {
                            it.copy(isLoading = false, error = "Couldn't load this movie. Check your connection and try again.")
                        }
                    }
                }
        }
    }

    private fun onMovieLoaded(movie: MovieModel) {
        _uiState.update { it.copy(movie = movie, isLoading = false, error = null) }
        val uid = authRepository.currentUserId ?: return
        viewModelScope.launch {
            favoritesRepository.isFavorite(uid, movie.id).collect { fav ->
                _uiState.update { it.copy(isFavorite = fav) }
            }
        }
    }

    fun toggleFavorite() {
        val uid = authRepository.currentUserId ?: return
        val movie = _uiState.value.movie ?: return
        val isFav = _uiState.value.isFavorite
        viewModelScope.launch {
            if (isFav) {
                favoritesRepository.removeFavorite(uid, movie.id)
            } else {
                favoritesRepository.addFavorite(uid, movie)
            }
        }
    }
}