package com.example.movie_project.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.movie_project.data.local.MovieLogDao
import com.example.movie_project.data.local.MovieLogEntry
import com.example.movie_project.util.NetworkMonitor
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Repository acting as the single source of truth for Movie Log.
 *
 * Strategy:
 * - Reads always come from Room (offline-safe).
 * - Writes go to Room first, then attempt Firebase if online; otherwise queued via pendingSync.
 * - A real-time Firebase listener mirrors remote changes into Room when online.
 *
 * Firebase = source of truth on conflict (timestamp-based).
 * Room    = local cache + offline write queue.
 */
class MovieLogRepository(
    private val movieLogDao: MovieLogDao,
    private val networkMonitor: NetworkMonitor,
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private var firebaseRef: DatabaseReference? = null
    private var firebaseListener: ValueEventListener? = null
    private var listeningForUserId: String? = null

    // ----- Reads -----

    /**
     * Observe movie log entries for a user from Room (offline-safe).
     */
    fun observeEntries(userId: String): LiveData<List<MovieLogEntry>> {
        return movieLogDao.getEntriesForUser(userId)
    }

    /**
     * Observe a single movie log entry by ID.
     */
    fun getEntryById(userId: String, entryId: String): LiveData<MovieLogEntry?> {
        return movieLogDao.getEntryById(userId, entryId)
    }

    // ----- Writes -----

    /**
     * Add a movie log entry. Writes to Room immediately. If online, pushes to Firebase
     * and clears the pendingSync flag. If offline, leaves it in the queue.
     */
    suspend fun addEntry(userId: String, entry: MovieLogEntry) {
        val online = networkMonitor.isCurrentlyOnline()
        val finalEntry = entry.copy(
            userId = userId,
            pendingSync = !online,
            pendingDeletion = false,
            updatedAt = System.currentTimeMillis()
        )
        movieLogDao.upsert(finalEntry)

        if (online) {
            try {
                pushSingleToFirebase(userId, finalEntry)
                movieLogDao.clearPendingSync(userId, finalEntry.entryId)
            } catch (e: Exception) {
                Log.w(TAG, "addEntry: Firebase push failed, queued for sync", e)
                movieLogDao.upsert(finalEntry.copy(pendingSync = true))
            }
        }
    }

    /**
     * Update a movie log entry. Writes to Room immediately. If online, pushes to Firebase
     * and clears the pendingSync flag. If offline, leaves it in the queue.
     */
    suspend fun updateEntry(userId: String, entry: MovieLogEntry) {
        val online = networkMonitor.isCurrentlyOnline()
        val finalEntry = entry.copy(
            userId = userId,
            pendingSync = !online,
            updatedAt = System.currentTimeMillis()
        )
        movieLogDao.upsert(finalEntry)

        if (online) {
            try {
                pushSingleToFirebase(userId, finalEntry)
                movieLogDao.clearPendingSync(userId, finalEntry.entryId)
            } catch (e: Exception) {
                Log.w(TAG, "updateEntry: Firebase push failed, queued for sync", e)
                movieLogDao.upsert(finalEntry.copy(pendingSync = true))
            }
        }
    }

    /**
     * Delete a movie log entry. Soft-deletes in Room (UI hides it instantly). If online,
     * deletes from Firebase and hard-deletes locally. If offline, leaves it queued.
     */
    suspend fun deleteEntry(userId: String, entryId: String) {
        movieLogDao.markPendingDeletion(userId, entryId)

        if (networkMonitor.isCurrentlyOnline()) {
            try {
                deleteSingleFromFirebase(userId, entryId)
                movieLogDao.hardDelete(userId, entryId)
            } catch (e: Exception) {
                Log.w(TAG, "deleteEntry: Firebase delete failed, queued for sync", e)
                // Leave it in pendingDeletion state for the SyncManager
            }
        }
    }

    // ----- Sync operations -----

    /**
     * Push all locally-queued operations (adds/updates + deletes) to Firebase.
     * Called by SyncManager when connectivity is restored.
     */
    suspend fun pushPendingToFirebase(userId: String) {
        if (!networkMonitor.isCurrentlyOnline()) return

        val pending = movieLogDao.getPendingSyncForUser(userId)
        for (entry in pending) {
            try {
                if (entry.pendingDeletion) {
                    deleteSingleFromFirebase(userId, entry.entryId)
                    movieLogDao.hardDelete(userId, entry.entryId)
                } else if (entry.pendingSync) {
                    pushSingleToFirebase(userId, entry)
                    movieLogDao.clearPendingSync(userId, entry.entryId)
                }
            } catch (e: Exception) {
                Log.w(TAG, "pushPendingToFirebase: failed for ${entry.entryId}", e)
                // Leave in queue; will retry on next sync trigger
            }
        }
    }

    /**
     * Pull the full movie log snapshot from Firebase and replace the local cache.
     * Pending local operations are preserved by [MovieLogDao.replaceAllForUser]
     * with timestamp-based conflict resolution.
     */
    suspend fun pullFromFirebase(userId: String) {
        if (!networkMonitor.isCurrentlyOnline()) return

        try {
            val snapshot = readFirebaseSnapshot(userId)
            val entries = snapshotToEntries(userId, snapshot)
            movieLogDao.replaceAllForUser(userId, entries)
        } catch (e: Exception) {
            Log.e(TAG, "pullFromFirebase failed", e)
            _errorMessage.postValue(e.localizedMessage)
        }
    }

    /**
     * Start real-time Firebase listener for a user.
     * Mirrors remote changes into Room while online.
     */
    fun startFirebaseListener(userId: String) {
        // Already listening for this user — skip
        if (listeningForUserId == userId && firebaseListener != null) return

        stopFirebaseListener()
        listeningForUserId = userId
        firebaseRef = database.reference.child("movieLog").child(userId)

        firebaseListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch {
                    try {
                        val entries = snapshotToEntries(userId, snapshot)
                        movieLogDao.replaceAllForUser(userId, entries)
                    } catch (e: Exception) {
                        Log.e(TAG, "Firebase listener: replaceAll failed", e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase listener cancelled: ${error.message}")
                _errorMessage.postValue(error.message)
            }
        }

        firebaseRef?.addValueEventListener(firebaseListener!!)
    }

    /**
     * Stop the real-time Firebase listener (call on sign-out or VM cleared).
     */
    fun stopFirebaseListener() {
        firebaseListener?.let { listener ->
            firebaseRef?.removeEventListener(listener)
        }
        firebaseListener = null
        firebaseRef = null
        listeningForUserId = null
    }

    /**
     * Clear all locally-cached entries for a user (used on sign-out).
     */
    suspend fun clearLocalForUser(userId: String) {
        movieLogDao.clearForUser(userId)
    }

    // ----- Firebase helpers -----

    private suspend fun pushSingleToFirebase(userId: String, entry: MovieLogEntry) =
        suspendCancellableCoroutine { cont ->
            database.reference
                .child("movieLog")
                .child(userId)
                .child(entry.entryId)
                .setValue(entry)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    private suspend fun deleteSingleFromFirebase(userId: String, entryId: String) =
        suspendCancellableCoroutine { cont ->
            database.reference
                .child("movieLog")
                .child(userId)
                .child(entryId)
                .removeValue()
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    private suspend fun readFirebaseSnapshot(userId: String): DataSnapshot =
        suspendCancellableCoroutine { cont ->
            database.reference
                .child("movieLog")
                .child(userId)
                .get()
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    private fun snapshotToEntries(userId: String, snapshot: DataSnapshot): List<MovieLogEntry> {
        val list = mutableListOf<MovieLogEntry>()
        if (!snapshot.exists()) return list
        for (child in snapshot.children) {
            val entry = child.getValue(MovieLogEntry::class.java) ?: continue
            list.add(entry.copy(
                userId = userId,
                pendingSync = false,
                pendingDeletion = false
            ))
        }
        return list
    }

    companion object {
        private const val TAG = "MovieLogRepository"
    }
}