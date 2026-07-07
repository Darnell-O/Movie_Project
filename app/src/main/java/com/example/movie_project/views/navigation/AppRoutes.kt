package com.example.movie_project.views.navigation

import android.net.Uri
import com.example.movie_project.models.domain.MovieModel

object Route {
    // Auth
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"

    // Bottom nav tabs
    const val HOME = "home"
    const val FAVORITES = "favorites"
    const val SEARCH = "search"
    const val MOVIE_LOG = "movie_log"

    // Detail — individual fields encoded as query params
    const val DETAIL = "detail?movieId={movieId}&title={title}&posterPath={posterPath}&overview={overview}&releaseDate={releaseDate}&voteAverage={voteAverage}"

    // MovieLogDetail — entryId is optional (null = new entry)
    const val MOVIE_LOG_DETAIL = "movie_log_detail?entryId={entryId}"

    // Top-level screens
    const val PROFILE = "profile"
    const val USERS = "users"

    // Builder helpers
    fun detail(movie: MovieModel) =
        "detail" +
        "?movieId=${movie.id}" +
        "&title=${Uri.encode(movie.title ?: "")}" +
        "&posterPath=${Uri.encode(movie.posterPath ?: "")}" +
        "&overview=${Uri.encode(movie.overview ?: "")}" +
        "&releaseDate=${Uri.encode(movie.releaseDate ?: "")}" +
        "&voteAverage=${movie.voteAverage ?: 0f}"

    fun movieLogDetail(entryId: String? = null) =
        if (entryId != null) "movie_log_detail?entryId=$entryId"
        else "movie_log_detail"
}
