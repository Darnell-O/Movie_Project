package com.example.movie_project.views.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_project.models.MovieModel
import com.example.movie_project.networking.ApiUtil
import com.example.movie_project.util.ApiKeyProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class SearchViewModel : ViewModel() {
    private val _searchMovies = MutableStateFlow<List<MovieModel>>(emptyList())
    val searchMovies: StateFlow<List<MovieModel>> = _searchMovies.asStateFlow()

    private val _filteredMovies = MutableStateFlow<List<MovieModel>>(emptyList())
    val filteredMovies: StateFlow<List<MovieModel>> = _filteredMovies.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val searchApiService = ApiUtil.apiService

    fun searchMovies(query: String) {
        _errorMessage.value = null // Clear previous error
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = searchApiService.searchMovies(ApiKeyProvider.getApiKey(), query)
                if (response.isSuccessful) {
                    _searchMovies.value = response.body()?.results ?: emptyList()
                    Log.i("SearchViewModel", "Success: ${response.body()?.results}")
                }
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error: ${e.message}")
                _errorMessage.value = e.message ?: "An unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterList(text: String) {
        val filteredList = _searchMovies.value.filter { item ->
            item.title?.lowercase(Locale.getDefault())
                ?.contains(text.lowercase(Locale.getDefault())) == true
        }
        _filteredMovies.value = filteredList
    }
}