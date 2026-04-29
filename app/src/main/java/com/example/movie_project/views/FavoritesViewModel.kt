package com.example.movie_project.views

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.movie_project.MovieMagicApp
import com.example.movie_project.data.repository.FavoritesRepository
import com.example.movie_project.models.MovieModel
import com.google.firebase.auth.FirebaseAuth

/**
 * ViewModel for the Favorites screen.
 *
 * Reads come from the local Room cache (offline-safe). The repository's
 * Firebase listener mirrors remote updates into Room when online.
 */
class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FavoritesRepository =
        MovieMagicApp.from(application).favoritesRepository

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Favorites list driven by Room — always available, online or offline.
     */
    private val _favorites = MediatorLiveData<List<MovieModel>>()
    val favorites: LiveData<List<MovieModel>> = _favorites

    private val repoErrorObserver = Observer<String?> { msg ->
        if (!msg.isNullOrEmpty()) _errorMessage.postValue(msg)
    }

    init {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId != null) {
            _isLoading.postValue(true)

            // Mirror Room → favorites LiveData
            _favorites.addSource(repository.observeFavorites(currentUserId)) { list ->
                _isLoading.postValue(false)
                _favorites.value = list
            }

            // Start real-time Firebase listener so Room stays fresh while online
            repository.startFirebaseListener(currentUserId)
        } else {
            _favorites.value = emptyList()
            _errorMessage.postValue("Please sign in to view favorites")
            _isLoading.postValue(false)
        }

        // Forward repository errors to the UI
        repository.errorMessage.observeForever(repoErrorObserver)
    }

    override fun onCleared() {
        super.onCleared()
        repository.errorMessage.removeObserver(repoErrorObserver)
        // Listener is app-scoped via the repository singleton — keep it running so
        // Room stays fresh as the user navigates. It is stopped explicitly on sign-out.
    }
}