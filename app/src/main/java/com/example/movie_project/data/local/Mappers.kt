package com.example.movie_project.data.local

import com.example.movie_project.models.MovieModel

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
    posterPath = poster_path,
    voteAverage = voteAverage,
    releaseDate = release_date,
    poster = poster,
    pendingSync = pendingSync,
    pendingDeletion = pendingDeletion,
    updatedAt = System.currentTimeMillis()
)

fun FavoriteEntry.toMovieModel(): MovieModel = MovieModel(
    id = movieId,
    title = title,
    overview = overview,
    poster_path = posterPath,
    voteAverage = voteAverage,
    release_date = releaseDate,
    poster = poster
)