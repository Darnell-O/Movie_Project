package com.example.movie_project.models.domain

/**
 * Domain/UI model for a movie.
 *
 * Used across ViewModels, repositories, and Compose UI. Deliberately free of any
 * network (Gson) or Firebase annotations — mapping from the network layer happens
 * via [com.example.movie_project.models.dto.MovieDto.toMovieModel], and the
 * Firebase boundary uses its own DTO
 * ([com.example.movie_project.data.repository.FavoriteRemoteDto]).
 */
data class MovieModel(
    val id: Int = 0,
    val title: String? = "",
    val overview: String? = "",
    val posterPath: String? = "",
    val voteAverage: Float? = 0.0f,
    val releaseDate: String? = "",
) {
    /** Full TMDB poster URL, or null when there's no poster path. */
    val posterUrl: String?
        get() = posterPath?.takeIf { it.isNotBlank() }?.let { "$POSTER_BASE_URL$it" }

    companion object {
        const val POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500"
    }
}