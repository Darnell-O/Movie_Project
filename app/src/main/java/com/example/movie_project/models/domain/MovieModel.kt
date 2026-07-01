package com.example.movie_project.models.domain

import java.io.Serializable

/**
 * Domain/UI model for a movie.
 *
 * This is the type used across ViewModels, repositories, adapters, Compose UI,
 * and is passed between screens as a [Serializable] intent extra. It is
 * deliberately free of any network (Gson) annotations — mapping from the
 * network layer happens via [com.example.movie_project.models.dto.MovieDto.toMovieModel].
 */
data class MovieModel(
    val id: Int = 0,
    val title: String? = "",
    val overview: String? = "",
    val poster_path: String? = "",
    val voteAverage: Float? = 0.0f,
    val release_date: String? = "",
    var poster: String = "https://image.tmdb.org/t/p/w500$poster_path"
) : Serializable