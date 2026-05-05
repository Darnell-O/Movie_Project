package com.example.movie_project.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

/**
 * Data Access Object for the movie_log table.
 * Supports offline-first reads, writes, and sync queue operations.
 * All operations are user-scoped for multi-user support.
 */
@Dao
interface MovieLogDao {

    /**
     * Returns all non-deleted entries for a given user, ordered by most recently added.
     */
    @Query("SELECT * FROM movie_log WHERE userId = :userId AND pendingDeletion = 0 ORDER BY dateAdded DESC")
    fun getEntriesForUser(userId: String): LiveData<List<MovieLogEntry>>

    /**
     * Returns a single entry by userId and entryId (not pending deletion).
     */
    @Query("SELECT * FROM movie_log WHERE userId = :userId AND entryId = :entryId AND pendingDeletion = 0")
    fun getEntryById(userId: String, entryId: String): LiveData<MovieLogEntry?>

    /**
     * Returns all entries that need to be synced to Firebase (added/updated or deleted offline).
     */
    @Query("SELECT * FROM movie_log WHERE userId = :userId AND (pendingSync = 1 OR pendingDeletion = 1)")
    suspend fun getPendingSyncForUser(userId: String): List<MovieLogEntry>

    /**
     * Insert or replace an entry.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: MovieLogEntry)

    /**
     * Soft-delete: marks an entry as pending deletion so it's hidden from the UI
     * but retained until synced to Firebase.
     */
    @Query("UPDATE movie_log SET pendingDeletion = 1, pendingSync = 0, updatedAt = :now WHERE userId = :userId AND entryId = :entryId")
    suspend fun markPendingDeletion(userId: String, entryId: String, now: Long = System.currentTimeMillis())

    /**
     * Hard-delete: permanently removes an entry after it has been synced.
     */
    @Query("DELETE FROM movie_log WHERE userId = :userId AND entryId = :entryId")
    suspend fun hardDelete(userId: String, entryId: String)

    /**
     * Clears the pendingSync flag after a successful push to Firebase.
     */
    @Query("UPDATE movie_log SET pendingSync = 0 WHERE userId = :userId AND entryId = :entryId")
    suspend fun clearPendingSync(userId: String, entryId: String)

    /**
     * Removes all entries for a user (used during full refresh from Firebase).
     */
    @Query("DELETE FROM movie_log WHERE userId = :userId")
    suspend fun clearForUser(userId: String)

    /**
     * Returns snapshot of all entries for a user (used in conflict resolution).
     */
    @Query("SELECT * FROM movie_log WHERE userId = :userId AND pendingDeletion = 0")
    suspend fun getEntriesSnapshot(userId: String): List<MovieLogEntry>

    /**
     * Replaces the entire local cache for a user with a fresh set from Firebase.
     * Preserves any pending operations by merging them back with timestamp-based conflict resolution.
     */
    @Transaction
    suspend fun replaceAllForUser(userId: String, entries: List<MovieLogEntry>) {
        // Save pending operations before clearing
        val pendingOps = getPendingSyncForUser(userId)
        clearForUser(userId)

        // Insert fresh data from Firebase
        entries.forEach { upsert(it) }

        // Re-apply pending operations with timestamp-based conflict resolution
        pendingOps.forEach { pending ->
            val firebaseEntry = entries.find { it.entryId == pending.entryId }

            if (pending.pendingDeletion) {
                // If marked for deletion locally, check timestamp
                if (firebaseEntry != null) {
                    if (pending.updatedAt > firebaseEntry.updatedAt) {
                        // Local deletion is newer - keep the deletion
                        markPendingDeletion(userId, pending.entryId)
                    }
                    // else: Firebase version is newer, deletion is obsolete
                }
            } else if (pending.pendingSync) {
                // Local edit/add pending
                if (firebaseEntry != null) {
                    // Conflict: compare timestamps
                    if (pending.updatedAt > firebaseEntry.updatedAt) {
                        // Local version is newer - keep it
                        upsert(pending)
                    }
                    // else: Firebase version is newer, already inserted above
                } else {
                    // New local entry not in Firebase yet
                    upsert(pending)
                }
            }
        }
    }
}