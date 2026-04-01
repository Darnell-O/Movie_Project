package com.example.movie_project.networking

import com.example.movie_project.models.MovieResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieService {

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