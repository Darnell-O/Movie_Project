package com.example.movie_project.views.detail

import androidx.lifecycle.ViewModel
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

data class DetailUiState(
    val movie: MovieModel? = null,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadMovie(movie: MovieModel) {
        _uiState.update { it.copy(movie = movie) }
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            favoritesRepository.isFavorite(uid, movie.id).collect { fav ->
                _uiState.update { it.copy(isFavorite = fav) }
            }
        }
    }

    fun toggleFavorite() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
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
