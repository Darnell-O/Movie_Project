package com.example.movie_project.views

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_project.models.MovieModel
import com.example.movie_project.networking.ApiUtil
import com.example.movie_project.networking.MovieService
import com.example.movie_project.util.ApiKeyProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class HomeViewModel : ViewModel() {
    private val _movies = MutableLiveData<List<MovieModel>>()
    val movies: LiveData<List<MovieModel>> = _movies

    private val apiService = ApiUtil.apiService

    init {
        fetchMovies()
    }

    fun fetchMovies() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getPopularMovies(ApiKeyProvider.getApiKey())
                if (response.isSuccessful) {
                    _movies.postValue(response.body()?.results)
                    Log.i("HomeViewModel", "Success: ${response.body()?.results}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("HomeViewModel", "Error: ${e.message}")
            }

        }
    }

    private fun retrievedMovies(movieList: List<MovieModel>) {
        _movies.value = movieList
    }
}

