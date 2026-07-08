package com.example.movie_project.data.local

import com.example.movie_project.models.domain.MovieModel

/**
 * Extension functions to convert between MovieModel and FavoriteEntry.
 */
fun MovieModel.toFavoriteEntry(
    userId: String,
    pendingSync: Boolean = false,
    pendingDeletion: Boolean = false
): FavoriteEntry = FavoriteEntry(
    userId = userId,
    movieId = id,
    title = title,
    overview = overview,
    posterPath = posterPath,
    voteAverage = voteAverage,
    releaseDate = releaseDate,
    poster = posterUrl ?: "",
    pendingSync = pendingSync,
    pendingDeletion = pendingDeletion,
    updatedAt = System.currentTimeMillis()
)

fun FavoriteEntry.toMovieModel(): MovieModel = MovieModel(
    id = movieId,
    title = title,
    overview = overview,
    posterPath = posterPath,
    voteAverage = voteAverage,
    releaseDate = releaseDate,
)