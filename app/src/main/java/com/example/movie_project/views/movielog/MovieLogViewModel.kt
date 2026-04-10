package com.example.movie_project.views.movielog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.movie_project.data.local.MovieLogDatabase
import com.example.movie_project.data.local.MovieLogEntry
import com.example.movie_project.data.repository.MovieLogRepository

/**
 * ViewModel for MovieLogFragment.
 * Provides all movie log entries to the UI via LiveData.
 */
class MovieLogViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MovieLogRepository

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    val allEntries: LiveData<List<MovieLogEntry>>

    init {
        val dao = MovieLogDatabase.getDatabase(application).movieLogDao()
        repository = MovieLogRepository(dao)
        allEntries = repository.allEntries
    }
}
