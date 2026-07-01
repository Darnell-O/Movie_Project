package com.example.movie_project.views.search

import com.example.movie_project.models.domain.MovieModel

/**
 * Immutable UI state for the Search screen.
 *
 * Exposing a single state object (rather than several independent streams)
 * keeps the screen's state consistent and is the idiomatic approach for
 * Compose-based MVVM.
 */
data class SearchUiState(
    val query: String = "",
    val movies: List<MovieModel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    /** True when there are no results to display (used for the empty placeholder). */
    val isEmpty: Boolean get() = movies.isEmpty()
}