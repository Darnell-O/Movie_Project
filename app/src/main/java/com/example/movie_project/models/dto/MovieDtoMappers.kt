package com.example.movie_project.models.dto

import com.example.movie_project.models.domain.MovieModel

/**
 * Maps a network [MovieDto] to the domain [MovieModel] used by the rest of the app.
 *
 * The domain model's `poster` URL is derived from `poster_path` via its
 * constructor default, matching the existing image-loading convention.
 */
fun MovieDto.toMovieModel(): MovieModel = MovieModel(
    id = id,
    title = title,
    overview = overview,
    poster_path = posterPath,
    voteAverage = voteAverage,
    release_date = releaseDate,
)

/**
 * Maps a list of [MovieDto] to a list of domain [MovieModel].
 */
fun List<MovieDto>.toMovieModels(): List<MovieModel> = map { it.toMovieModel() }