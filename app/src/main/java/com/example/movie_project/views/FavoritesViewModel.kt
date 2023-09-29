package com.example.movie_project.views

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.movie_project.models.MovieModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FavoritesViewModel : ViewModel() {
    private val _favorites = MutableLiveData<List<MovieModel>>()
    val favorites: MutableLiveData<List<MovieModel>> = _favorites
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var favDatabaseReference: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("favorites").child(userId.toString())


     fun fetchFavorites() {
        val favoriteMoviesList = mutableListOf<MovieModel>()
        favDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue()
                if (snapshot.exists()) {
                    val itemList = snapshot.children.toList()
                    if (itemList != null) {
                        for (movie in itemList) {
                            val movieItem = movie.getValue(MovieModel::class.java)
                            movieItem?.let { favoriteMoviesList.add(it) }
                        }
                        _favorites.postValue(favoriteMoviesList)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FavoritesViewModel", "Error: ${error.message}")
            }
        })
    }
}