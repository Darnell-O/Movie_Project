package com.example.movie_project.views

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_project.data.repository.MovieRepository
import com.example.movie_project.models.domain.MovieModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : ViewModel() {
    private val _movies = MutableLiveData<List<MovieModel>>()
    val movies: LiveData<List<MovieModel>> = _movies

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        fetchMovies()
    }

    fun fetchMovies() {
        viewModelScope.launch {
            _errorMessage.value = null
            _isLoading.value = true

            val result = movieRepository.getPopularMovies()
            result.fold(
                onSuccess = { movies -> _movies.value = movies },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Failed to load movies"
                }
            )

            _isLoading.value = false
        }
    }
}
