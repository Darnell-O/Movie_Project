package com.example.movie_project.models

import java.io.Serializable
data class MovieModel(
    val id: Int = 0,
    val title: String? = "",
    val overview: String? = "",
    val poster_path: String? = "",
    val voteAverage: Float? = 0.0f,
    val release_date: String? = "",
    var poster: String = "https://image.tmdb.org/t/p/w500$poster_path"
): Serializable


