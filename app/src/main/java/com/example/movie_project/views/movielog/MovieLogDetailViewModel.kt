package com.example.movie_project.views.movielog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.movie_project.data.local.MovieLogDatabase
import com.example.movie_project.data.local.MovieLogEntry
import com.example.movie_project.data.repository.MovieLogRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for MovieLogDetailActivity.
 * Handles loading, saving, and updating movie log entries via the repository.
 */
class MovieLogDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MovieLogRepository

    init {
        val dao = MovieLogDatabase.getDatabase(application).movieLogDao()
        repository = MovieLogRepository(dao)
    }

    /**
     * Loads a single movie log entry by ID for editing.
     */
    fun getEntryById(id: Long): LiveData<MovieLogEntry> {
        return repository.getEntryById(id)
    }

    /**
     * Inserts a new movie log entry.
     * @param entry the entry to insert
     * @param onComplete callback invoked after successful insertion
     */
    fun insertEntry(entry: MovieLogEntry, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insertEntry(entry)
            onComplete()
        }
    }

    /**
     * Updates an existing movie log entry.
     * @param entry the entry to update
     * @param onComplete callback invoked after successful update
     */
    fun updateEntry(entry: MovieLogEntry, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.updateEntry(entry)
            onComplete()
        }
    }
}
