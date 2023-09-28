package com.example.movie_project.views

import android.util.Log
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_project.databinding.ActivityMainBinding
import com.example.movie_project.databinding.FragmentSearchBinding
import com.example.movie_project.models.MovieModel
import com.example.movie_project.networking.ApiUtil
import com.example.movie_project.networking.ApiUtil.apiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val _searchMovies = MutableLiveData<List<MovieModel>>()
    val searchMovies: LiveData<List<MovieModel>> = _searchMovies
    private val searchAdapter = SearchAdapter(arrayListOf())

    private val searchApiService = ApiUtil.apiService
    private lateinit var binding: FragmentSearchBinding

    init {
        searchMovies("")
    }

     fun searchMovies(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = searchApiService.searchMovies("1dd5fc6831acffaa5cb5999a57c389c7", query)
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
        val filteredList = ArrayList<MovieModel>()
        for (item in searchMovies.value!!) {
            if (item.title?.toLowerCase()?.contains(text.toLowerCase()) == true) {
                filteredList.add(item)
            }
        }
        searchAdapter.filterList(filteredList)

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