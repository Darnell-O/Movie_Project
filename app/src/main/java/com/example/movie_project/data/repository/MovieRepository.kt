package com.example.movie_project.data.repository

import com.example.movie_project.models.domain.MovieModel
import com.example.movie_project.models.dto.toMovieModels
import com.example.movie_project.networking.ApiUtil
import com.example.movie_project.networking.MovieService
import com.example.movie_project.util.ApiKeyProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository acting as the single source of truth for remote movie data (TMDB).
 *
 * Keeps the network/Retrofit details out of the ViewModels, mirroring the
 * pattern already used by [FavoritesRepository] and [MovieLogRepository] for
 * local data. ViewModels depend on this abstraction, not on Retrofit directly,
 * which keeps them testable (the repository can be faked/mocked).
 */
class MovieRepository(
    private val movieService: MovieService = ApiUtil.apiService,
    private val apiKeyProvider: () -> String = { ApiKeyProvider.getApiKey() },
) {

    /**
     * Search movies by query. Network call runs on the IO dispatcher.
     *
     * @return [Result.success] with the list of movies (possibly empty), or
     *         [Result.failure] carrying the thrown exception.
     */
    suspend fun searchMovies(query: String): Result<List<MovieModel>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = movieService.searchMovies(apiKeyProvider(), query)
                if (response.isSuccessful) {
                    response.body()?.results?.toMovieModels() ?: emptyList()
                } else {
                    throw IllegalStateException("Search failed: ${response.code()}")
                }
            }
        }

    /**
     * Fetch the list of currently popular movies.
     */
    suspend fun getPopularMovies(): Result<List<MovieModel>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = movieService.getPopularMovies(apiKeyProvider())
                if (response.isSuccessful) {
                    response.body()?.results?.toMovieModels() ?: emptyList()
                } else {
                    throw IllegalStateException("Failed to load movies: ${response.code()}")
                }
            }
        }
}