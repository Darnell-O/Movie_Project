package com.example.movie_project.data.local

import androidx.room.Entity
import androidx.room.Index

/**
 * Room Entity representing a user's favorite movie.
 * Composite primary key (userId + movieId) ensures per-user isolation.
 * pendingSync / pendingDeletion flags support offline-first writes.
 */
@Entity(
    tableName = "favorites",
    primaryKeys = ["userId", "movieId"],
    indices = [Index("userId")]
)
data class FavoriteEntry(
    val userId: String,
    val movieId: Int,
    val title: String? = "",
    val overview: String? = "",
    val posterPath: String? = "",
    val voteAverage: Float? = 0.0f,
    val releaseDate: String? = "",
    val poster: String = "",
    // Sync metadata
    val pendingSync: Boolean = false,
    val pendingDeletion: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)