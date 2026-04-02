package com.example.movie_project.views.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_project.models.MovieModel
import com.example.movie_project.networking.ApiUtil
import com.example.movie_project.util.ApiKeyProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class SearchViewModel : ViewModel() {
    private val _searchMovies = MutableLiveData<List<MovieModel>>()
    val searchMovies: LiveData<List<MovieModel>> = _searchMovies

    private val _filteredMovies = MutableLiveData<List<MovieModel>>()
    val filteredMovies: LiveData<List<MovieModel>> = _filteredMovies

    private val searchApiService = ApiUtil.apiService

     fun searchMovies(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = searchApiService.searchMovies(ApiKeyProvider.getApiKey(), query)
                if (response.isSuccessful) {
                    _searchMovies.postValue(response.body()?.results)
                    Log.i("SearchViewModel", "Success: ${response.body()?.results}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("SearchViewModel", "Error: ${e.message}")
            }

        }
    }





    fun filterList(text: String) {
        val filteredList = searchMovies.value.orEmpty().filter { item ->
            item.title?.lowercase(Locale.getDefault())
                ?.contains(text.lowercase(Locale.getDefault())) == true
        }
        _filteredMovies.postValue(filteredList)

//        if (filteredList.isEmpty()) {
//            Toast.makeText(this, "No Results Found", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(this, "Results Found", Toast.LENGTH_SHORT).show()
//        }
    }

//  fun query(text:String):String {
//      binding.searchView.setOnQueryTextListener(object :
//          SearchView.OnQueryTextListener {
//          override fun onQueryTextSubmit(query: String?): Boolean {
//              filterList(query.toString())
//              return false
//          }
//
//          override fun onQueryTextChange(newText: String?): Boolean {
//              filterList(newText.toString())
//              return false
//          }
//      })
//
//      return binding.searchView.query.toString()
//
//  }

}