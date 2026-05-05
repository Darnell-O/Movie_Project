package com.example.movie_project.views.movielog

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.movie_project.MovieMagicApp
import com.example.movie_project.data.local.MovieLogEntry
import com.example.movie_project.data.repository.MovieLogRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * ViewModel for MovieLogDetailActivity.
 * Handles loading, saving, updating, and deleting movie log entries via the repository.
 * Uses Firebase Auth userId for all operations.
 */
class MovieLogDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MovieLogRepository =
        MovieMagicApp.from(application).movieLogRepository

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val userId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    /**
     * Loads a single movie log entry by entryId for editing.
     */
    fun getEntryById(entryId: String): LiveData<MovieLogEntry?> {
        val uid = userId ?: run {
            _errorMessage.postValue("Please sign in to view entries")
            return MutableLiveData(null)
        }
        return repository.getEntryById(uid, entryId)
    }

    /**
     * Inserts a new movie log entry.
     * @param entry the entry to insert (userId will be set automatically)
     * @param onComplete callback invoked after successful insertion
     */
    fun insertEntry(entry: MovieLogEntry, onComplete: () -> Unit) {
        val uid = userId ?: run {
            _errorMessage.postValue("Please sign in to save entries")
            return
        }
        viewModelScope.launch {
            try {
                repository.addEntry(uid, entry)
                onComplete()
            } catch (e: Exception) {
                Log.e("MovieLogDetailVM", "Insert failed: ${e.message}")
                _errorMessage.postValue(e.localizedMessage ?: "Failed to save movie log entry")
            }
        }
    }

    /**
     * Updates an existing movie log entry.
     * @param entry the entry to update (userId will be set automatically)
     * @param onComplete callback invoked after successful update
     */
    fun updateEntry(entry: MovieLogEntry, onComplete: () -> Unit) {
        val uid = userId ?: run {
            _errorMessage.postValue("Please sign in to update entries")
            return
        }
        viewModelScope.launch {
            try {
                repository.updateEntry(uid, entry)
                onComplete()
            } catch (e: Exception) {
                Log.e("MovieLogDetailVM", "Update failed: ${e.message}")
                _errorMessage.postValue(e.localizedMessage ?: "Failed to update movie log entry")
            }
        }
    }

    /**
     * Deletes a movie log entry.
     * @param entryId the ID of the entry to delete
     * @param onComplete callback invoked after successful deletion
     */
    fun deleteEntry(entryId: String, onComplete: () -> Unit) {
        val uid = userId ?: run {
            _errorMessage.postValue("Please sign in to delete entries")
            return
        }
        viewModelScope.launch {
            try {
                repository.deleteEntry(uid, entryId)
                onComplete()
            } catch (e: Exception) {
                Log.e("MovieLogDetailVM", "Delete failed: ${e.message}")
                _errorMessage.postValue(e.localizedMessage ?: "Failed to delete movie log entry")
            }
        }
    }
}
