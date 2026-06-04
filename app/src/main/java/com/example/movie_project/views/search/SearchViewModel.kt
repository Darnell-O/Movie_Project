package com.example.movie_project.views.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.movie_project.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Search screen.
 *
 * - Depends on [MovieRepository] (constructor-injected) rather than Retrofit
 *   directly, keeping the data source swappable and the VM unit-testable.
 * - Exposes a single [SearchUiState] via [StateFlow] for the Compose UI to
 *   collect.
 */
class SearchViewModel(
    private val movieRepository: MovieRepository = MovieRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    /** Update the current query text (called as the user types). */
    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    /** Execute a search using the current query. */
    fun search() {
        val query = _uiState.value.query
        if (query.isBlank()) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = movieRepository.searchMovies(query)
            _uiState.update { state ->
                result.fold(
                    onSuccess = { movies ->
                        state.copy(movies = movies, isLoading = false)
                    },
                    onFailure = { error ->
                        state.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "An unknown error occurred"
                        )
                    }
                )
            }
        }
    }

    /** Clear a consumed error message so it isn't shown again on recomposition. */
    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Factory enabling constructor injection of [MovieRepository] while still
     * working with the standard `by viewModels()` delegate.
     */
    class Factory(
        private val movieRepository: MovieRepository = MovieRepository(),
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return SearchViewModel(movieRepository) as T
        }
    }
}