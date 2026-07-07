package com.example.movie_project.data.repository

import com.example.movie_project.data.local.FavoriteEntry

/**
 * Firebase representation of a favorite. Uses the LEGACY field names
 * (poster_path, release_date, poster) so existing remote data — written before
 * [com.example.movie_project.models.domain.MovieModel] was cleaned up — still
 * deserializes. This type never leaks past the repository boundary.
 */
data class FavoriteRemoteDto(
    val id: Int = 0,
    val title: String? = "",
    val overview: String? = "",
    val poster_path: String? = "",
    val voteAverage: Float? = 0.0f,
    val release_date: String? = "",
    val poster: String = ""
)

fun FavoriteEntry.toRemoteDto() = FavoriteRemoteDto(
    id = movieId,
    title = title,
    overview = overview,
    poster_path = posterPath,
    voteAverage = voteAverage,
    release_date = releaseDate,
    poster = poster
)

fun FavoriteRemoteDto.toFavoriteEntry(userId: String) = FavoriteEntry(
    userId = userId,
    movieId = id,
    title = title,
    overview = overview,
    posterPath = poster_path,
    voteAverage = voteAverage,
    releaseDate = release_date,
    poster = poster,
    pendingSync = false,
    pendingDeletion = false
)