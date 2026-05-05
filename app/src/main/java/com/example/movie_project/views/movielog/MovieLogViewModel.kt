package com.example.movie_project.views.movielog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.movie_project.MovieMagicApp
import com.example.movie_project.data.local.MovieLogEntry
import com.example.movie_project.data.repository.MovieLogRepository
import com.google.firebase.auth.FirebaseAuth

/**
 * ViewModel for MovieLogFragment.
 *
 * Reads come from the local Room cache (offline-safe). The repository's
 * Firebase listener mirrors remote updates into Room when online.
 */
class MovieLogViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MovieLogRepository =
        MovieMagicApp.from(application).movieLogRepository

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Movie log entries driven by Room — always available, online or offline.
     */
    private val _allEntries = MediatorLiveData<List<MovieLogEntry>>()
    val allEntries: LiveData<List<MovieLogEntry>> = _allEntries

    private val repoErrorObserver = Observer<String?> { msg ->
        if (!msg.isNullOrEmpty()) _errorMessage.postValue(msg)
    }

    init {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId != null) {
            _isLoading.postValue(true)

            // Mirror Room → allEntries LiveData
            _allEntries.addSource(repository.observeEntries(currentUserId)) { list ->
                _isLoading.postValue(false)
                _allEntries.value = list
            }

            // Start real-time Firebase listener so Room stays fresh while online
            repository.startFirebaseListener(currentUserId)
        } else {
            _allEntries.value = emptyList()
            _errorMessage.postValue("Please sign in to view your movie log")
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
