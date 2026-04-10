package com.example.movie_project.views

import android.util.Log
import androidx.lifecycle.LiveData
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

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Store reference to database and listener for proper cleanup
    private var favDatabaseReference: DatabaseReference? = null
    private var valueEventListener: ValueEventListener? = null


     fun fetchFavorites() {
        _errorMessage.postValue(null)
        _isLoading.postValue(true)

        // Get current user ID and check if user is authenticated
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        
        if (currentUserId == null) {
            Log.e("FavoritesViewModel", "User not authenticated")
            _errorMessage.postValue("Please sign in to view favorites")
            _isLoading.postValue(false)
            _favorites.postValue(emptyList())
            return
        }
        
        // Remove any existing listener to avoid duplicates
        removeListener()
        
        // Lazy initialization of database reference with authenticated user ID
        favDatabaseReference = FirebaseDatabase.getInstance()
            .reference
            .child("favorites")
            .child(currentUserId)
        
        // Create and store the listener for later removal
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val favoriteMoviesList = mutableListOf<MovieModel>()
                
                if (snapshot.exists()) {
                    for (movie in snapshot.children) {
                        val movieItem = movie.getValue(MovieModel::class.java)
                        movieItem?.let { favoriteMoviesList.add(it) }
                    }
                }
                
                _favorites.postValue(favoriteMoviesList)
                _isLoading.postValue(false)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FavoritesViewModel", "Error: ${error.message}")
                _errorMessage.postValue(error.message)
                _isLoading.postValue(false)
                _favorites.postValue(emptyList())
            }
        }
        
        // Add the listener for real-time updates
        favDatabaseReference?.addValueEventListener(valueEventListener!!)
    }
    
    private fun removeListener() {
        valueEventListener?.let { listener ->
            favDatabaseReference?.removeEventListener(listener)
        }
        valueEventListener = null
    }
    
    override fun onCleared() {
        super.onCleared()
        // Clean up listener when ViewModel is destroyed to prevent memory leaks
        removeListener()
    }
}
