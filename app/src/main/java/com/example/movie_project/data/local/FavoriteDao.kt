package com.example.movie_project.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

/**
 * Data Access Object for the favorites table.
 * Supports offline-first reads, writes, and sync queue operations.
 */
@Dao
interface FavoriteDao {

    /**
     * Returns all non-deleted favorites for a given user, ordered by most recently updated.
     */
    @Query("SELECT * FROM favorites WHERE userId = :userId AND pendingDeletion = 0 ORDER BY updatedAt DESC")
    fun getFavoritesForUser(userId: String): LiveData<List<FavoriteEntry>>

    /**
     * Returns all entries that need to be synced to Firebase (added or deleted offline).
     */
    @Query("SELECT * FROM favorites WHERE userId = :userId AND (pendingSync = 1 OR pendingDeletion = 1)")
    suspend fun getPendingSyncForUser(userId: String): List<FavoriteEntry>

    /**
     * Observes whether a specific movie is in the user's favorites (not pending deletion).
     */
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND movieId = :movieId AND pendingDeletion = 0)")
    fun isFavorite(userId: String, movieId: Int): LiveData<Boolean>

    /**
     * Insert or replace a favorite entry.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: FavoriteEntry)

    /**
     * Soft-delete: marks a favorite as pending deletion so it's hidden from the UI
     * but retained until synced to Firebase.
     */
    @Query("UPDATE favorites SET pendingDeletion = 1, pendingSync = 0, updatedAt = :now WHERE userId = :userId AND movieId = :movieId")
    suspend fun markPendingDeletion(userId: String, movieId: Int, now: Long = System.currentTimeMillis())

    /**
     * Hard-delete: permanently removes a favorite entry after it has been synced.
     */
    @Query("DELETE FROM favorites WHERE userId = :userId AND movieId = :movieId")
    suspend fun hardDelete(userId: String, movieId: Int)

    /**
     * Clears the pendingSync flag after a successful push to Firebase.
     */
    @Query("UPDATE favorites SET pendingSync = 0 WHERE userId = :userId AND movieId = :movieId")
    suspend fun clearPendingSync(userId: String, movieId: Int)

    /**
     * Removes all favorites for a user (used during full refresh from Firebase).
     */
    @Query("DELETE FROM favorites WHERE userId = :userId")
    suspend fun clearForUser(userId: String)

    /**
     * Replaces the entire local cache for a user with a fresh set from Firebase.
     * Preserves any pending operations by merging them back in after the replace.
     */
    @Transaction
    suspend fun replaceAllForUser(userId: String, entries: List<FavoriteEntry>) {
        // Save pending operations before clearing
        val pendingOps = getPendingSyncForUser(userId)
        clearForUser(userId)
        // Insert fresh data from Firebase
        entries.forEach { upsert(it) }
        // Re-apply any pending operations that weren't in the Firebase data
        pendingOps.forEach { pending ->
            val existsInFresh = entries.any { it.movieId == pending.movieId }
            if (pending.pendingDeletion) {
                // If it was pending deletion and Firebase still has it, re-mark for deletion
                if (existsInFresh) {
                    markPendingDeletion(userId, pending.movieId)
                }
                // If Firebase doesn't have it, the deletion already happened — skip
            } else if (pending.pendingSync && !existsInFresh) {
                // Offline add that Firebase doesn't know about yet — re-insert
                upsert(pending)
            }
        }
    }
}