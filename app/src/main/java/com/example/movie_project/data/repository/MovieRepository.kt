package com.example.movie_project.data.repository

import com.example.movie_project.di.IoDispatcher
import com.example.movie_project.models.domain.MovieModel
import com.example.movie_project.models.dto.toMovieModel
import com.example.movie_project.models.dto.toMovieModels
import com.example.movie_project.networking.MovieService
import com.example.movie_project.util.ApiKeyProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository acting as the single source of truth for remote movie data (TMDB).
 *
 * Keeps the network/Retrofit details out of the ViewModels, mirroring the
 * pattern already used by [FavoritesRepository] and [MovieLogRepository] for
 * local data. ViewModels depend on this abstraction, not on Retrofit directly,
 * which keeps them testable (the repository can be faked/mocked).
 */
@Singleton
class MovieRepository @Inject constructor(
    private val movieService: MovieService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private val apiKeyProvider: () -> String = { ApiKeyProvider.getApiKey() }


    /**
     * Search movies by query. Network call runs on the IO dispatcher.
     *
     * @return [Result.success] with the list of movies (possibly empty), or
     *         [Result.failure] carrying the thrown exception.
     */
    suspend fun searchMovies(query: String): Result<List<MovieModel>> =
        withContext(ioDispatcher) {
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
     * Fetch full details for a single movie by id.
     */
    suspend fun getMovieDetails(movieId: Int): Result<MovieModel> =
        withContext(ioDispatcher) {
            runCatching {
                val response = movieService.getMovieDetails(movieId, apiKeyProvider())
                if (response.isSuccessful) {
                    response.body()?.toMovieModel()
                        ?: throw IllegalStateException("Empty movie details")
                } else {
                    throw IllegalStateException("Failed to load movie: ${response.code()}")
                }
            }
        }

    /**
     * Fetch the list of currently popular movies.
     */
    suspend fun getPopularMovies(): Result<List<MovieModel>> =
        withContext(ioDispatcher) {
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