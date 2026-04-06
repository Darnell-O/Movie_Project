package com.example.movie_project.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a movie log entry.
 * Stores all details about a movie the user has watched or wants to watch.
 */
@Entity(tableName = "movie_log")
data class MovieLogEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val movieTitle: String = "",
    val year: String = "",
    val dateWatched: String = "",
    val directedBy: String = "",
    val starring: String = "",
    val rating: Int = 0,
    val inTheater: Boolean = false,
    val atHome: Boolean = false,
    val firstWatch: Boolean = false,
    val rewatch: Boolean = false,
    val alone: Boolean = false,
    val withSomeone: Boolean = false,
    val notes: String = "",
    val dateAdded: Long = System.currentTimeMillis()
)
