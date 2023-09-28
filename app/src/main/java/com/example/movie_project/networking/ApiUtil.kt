package com.example.movie_project.networking

import com.example.movie_project.models.MovieResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiUtil {
    const val BASE_URL = "https://api.themoviedb.org/3/"

    private val Api_KEY = "1dd5fc6831acffaa5cb5999a57c389c7"


    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

   val apiService: MovieService = retrofit.create(MovieService::class.java)
}



