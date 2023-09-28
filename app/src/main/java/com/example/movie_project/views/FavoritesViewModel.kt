package com.example.movie_project.views

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movie_project.models.MovieModel
import com.example.movie_project.networking.ApiUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoritesViewModel : ViewModel() {
    private val _favorites = MutableLiveData<List<MovieModel>>()
    val favorites: MutableLiveData<List<MovieModel>> = _favorites
    private val _favoriteMovies = mutableListOf<MovieModel>()
    val favoriteMovies: MutableList<MovieModel> = _favoriteMovies
    private val favoritesAdapter = FavoritesAdapter(arrayListOf())

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var favDatabaseReference: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("favorites").child(userId.toString())
    private val findByService = ApiUtil.apiService


    init {
        fetchFavorites()
    }

    fun fetchFavorites() {
        favDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favoriteMovies.clear()
                if (snapshot.exists()) {
                    val itemList = snapshot.children.toList()
                    if (itemList != null) {
                        for (movie in itemList) {
                            var movieItem = movie.getValue(MovieModel::class.java)
                            movieItem?.let { favoriteMovies.add(it) }
                            /** Try and take items in favoriteMovies and pass them into a MutableLiveDate
                             * ex: private val _movies = MutableLiveData<List<MovieModel>>()
                             *     val movies: LiveData<List<MovieModel>> = _movies
                             * */





//                            viewModelScope.launch(Dispatchers.IO) {
//                                try {
//                                    val movieDetailsList = mutableListOf<List<MovieModel>>()
//                                    for (movie in favoriteMovies) {
//                                        val movieId = movie.id
//                                        val favResponse = findByService.findById(
//                                            movieId.toString(),
//                                            "1dd5fc6831acffaa5cb5999a57c389c7"
//                                        )
//                                        if (favResponse.isSuccessful) {
//                                            val movieDetails = favResponse.body()?.results
//                                            movieDetailsList?.let {
//                                                if (movieDetails != null) {
//                                                    movieDetailsList.add(movieDetails)
//                                                }
//                                            }
//                                        }
//                                        _favorites.postValue(favResponse.body()?.results)
//                                        Log.i(
//                                            "FavoritesViewModel",
//                                            "Success: ${favResponse.body()?.results}"
//                                        )
//
//
//                                    }
//                                } catch (e: Exception) {
//                                    e.printStackTrace()
//                                    Log.e("FavoritesViewModel", "Error: ${e.message}")
//                                }
//                            }


                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FavoritesViewModel", "Error: ${error.message}")
            }
        })
    }

}