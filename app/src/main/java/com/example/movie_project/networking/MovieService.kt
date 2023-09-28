package com.example.movie_project.networking

import com.example.movie_project.models.MovieResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieService {
    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val API_Key = "1dd5fc6831acffaa5cb5999a57c389c7"
    }

    @GET("movie/popular")
    suspend fun getPopularMovies(@Query("api_key") apiKey: String?): Response<MovieResponse>

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String?,
        @Query("query") query: String?
    ): Response<MovieResponse>

    @GET("find/{external_id}")
    suspend fun findById(
        @Path("external_id") externalId: String,
        @Query("api_key") apiKey: String
    ): Response<MovieResponse>


}