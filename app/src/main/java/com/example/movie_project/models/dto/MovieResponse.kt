package com.example.movie_project.models.dto

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Network DTO wrapping a paginated list of movies returned by the TMDB API.
 */
data class MovieResponse(
    @SerializedName("page")
    @Expose
    val page: Int?,
    @SerializedName("results")
    @Expose
    val results: List<MovieDto>,
    @SerializedName("total_pages")
    @Expose
    val totalPages: Int?,
    @SerializedName("total_results")
    @Expose
    val totalResults: Int?
)