package com.example.movie_project.networking

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiUtil {
    const val BASE_URL = "https://api.themoviedb.org/3/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

   val apiService: MovieService = retrofit.create(MovieService::class.java)
}



