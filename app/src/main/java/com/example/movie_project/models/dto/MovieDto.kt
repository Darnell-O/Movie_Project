package com.example.movie_project.models.dto

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Network DTO representing a single movie as returned by the TMDB API.
 *
 * This type is confined to the network boundary (Retrofit + Gson). It is
 * mapped to the domain [com.example.movie_project.models.domain.MovieModel]
 * via [toMovieModel] before being exposed to repositories / ViewModels / UI.
 *
 * NOTE: [voteAverage] intentionally maps to the JSON key "voteAverage" to
 * preserve the original (pre-refactor) behavior. The actual TMDB key is
 * "vote_average"; correcting this is tracked as a follow-up so this refactor
 * stays behavior-preserving.
 */
data class MovieDto(
    @SerializedName("id")
    @Expose
    val id: Int = 0,
    @SerializedName("title")
    @Expose
    val title: String? = "",
    @SerializedName("overview")
    @Expose
    val overview: String? = "",
    @SerializedName("poster_path")
    @Expose
    val posterPath: String? = "",
    @SerializedName("voteAverage")
    @Expose
    val voteAverage: Float? = 0.0f,
    @SerializedName("release_date")
    @Expose
    val releaseDate: String? = "",
)