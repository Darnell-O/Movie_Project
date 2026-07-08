package com.example.movie_project.models.dto

import com.example.movie_project.models.domain.MovieModel

/**
 * Maps a network [MovieDto] to the domain [MovieModel] used by the rest of the app.
 *
 * The domain model's poster URL is derived from `posterPath` via [MovieModel.posterUrl].
 */
fun MovieDto.toMovieModel(): MovieModel = MovieModel(
    id = id,
    title = title,
    overview = overview,
    posterPath = posterPath,
    voteAverage = voteAverage,
    releaseDate = releaseDate,
)

/**
 * Maps a list of [MovieDto] to a list of domain [MovieModel].
 */
fun List<MovieDto>.toMovieModels(): List<MovieModel> = map { it.toMovieModel() }