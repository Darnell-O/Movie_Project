package com.example.movie_project.data.repository

import androidx.lifecycle.LiveData
import com.example.movie_project.data.local.MovieLogDao
import com.example.movie_project.data.local.MovieLogEntry

/**
 * Repository for Movie Log feature.
 * Acts as a single source of truth, abstracting the data layer from ViewModels.
 */
class MovieLogRepository(private val movieLogDao: MovieLogDao) {

    /**
     * Returns all movie log entries ordered by date added (newest first).
     */
    val allEntries: LiveData<List<MovieLogEntry>> = movieLogDao.getAllEntries()

    /**
     * Returns a single movie log entry by its ID.
     */
    fun getEntryById(id: Long): LiveData<MovieLogEntry> {
        return movieLogDao.getEntryById(id)
    }

    /**
     * Inserts a new movie log entry.
     * @return the row ID of the newly inserted entry.
     */
    suspend fun insertEntry(entry: MovieLogEntry): Long {
        return movieLogDao.insertEntry(entry)
    }

    /**
     * Updates an existing movie log entry.
     */
    suspend fun updateEntry(entry: MovieLogEntry) {
        movieLogDao.updateEntry(entry)
    }

    /**
     * Deletes a movie log entry.
     */
    suspend fun deleteEntry(entry: MovieLogEntry) {
        movieLogDao.deleteEntry(entry)
    }
}
