package com.example.movie_project.data.local

import androidx.room.Entity
import androidx.room.Index
import java.util.UUID

/**
 * Room Entity representing a movie log entry.
 * Stores all details about a movie the user has watched or wants to watch.
 * 
 * Multi-user support with composite primary key (userId + entryId).
 * Sync metadata supports offline-first writes and Firebase synchronization.
 */
@Entity(
    tableName = "movie_log",
    primaryKeys = ["userId", "entryId"],
    indices = [Index("userId")]
)
data class MovieLogEntry(
    val userId: String = "",
    val entryId: String = UUID.randomUUID().toString(),
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
    val dateAdded: Long = System.currentTimeMillis(),
    // Sync metadata
    val pendingSync: Boolean = false,
    val pendingDeletion: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)