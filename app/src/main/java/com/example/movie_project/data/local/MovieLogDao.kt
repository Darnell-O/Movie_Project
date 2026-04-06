package com.example.movie_project.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Data Access Object for the movie_log table.
 * Provides CRUD operations for MovieLogEntry.
 */
@Dao
interface MovieLogDao {

    @Query("SELECT * FROM movie_log ORDER BY dateAdded DESC")
    fun getAllEntries(): LiveData<List<MovieLogEntry>>

    @Query("SELECT * FROM movie_log WHERE id = :id")
    fun getEntryById(id: Long): LiveData<MovieLogEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: MovieLogEntry): Long

    @Update
    suspend fun updateEntry(entry: MovieLogEntry)

    @Delete
    suspend fun deleteEntry(entry: MovieLogEntry)
}
