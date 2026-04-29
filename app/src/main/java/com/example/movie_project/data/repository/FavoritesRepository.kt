package com.example.movie_project.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.example.movie_project.data.local.FavoriteDao
import com.example.movie_project.data.local.FavoriteEntry
import com.example.movie_project.data.local.toFavoriteEntry
import com.example.movie_project.data.local.toMovieModel
import com.example.movie_project.models.MovieModel
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
 * Repository acting as the single source of truth for favorites.
 *
 * Strategy:
 * - Reads always come from Room (offline-safe).
 * - Writes go to Room first, then attempt Firebase if online; otherwise queued via pendingSync.
 * - A real-time Firebase listener mirrors remote changes into Room when online.
 *
 * Firebase = source of truth on conflict.
 * Room    = local cache + offline write queue.
 */
class FavoritesRepository(
    private val favoriteDao: FavoriteDao,
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
     * Observe favorites for a user from Room (offline-safe).
     */
    fun observeFavorites(userId: String): LiveData<List<MovieModel>> {
        return favoriteDao.getFavoritesForUser(userId).map { entries ->
            entries.map { it.toMovieModel() }
        }
    }

    /**
     * Observe whether a single movie is in the user's favorites.
     */
    fun isFavorite(userId: String, movieId: Int): LiveData<Boolean> {
        return favoriteDao.isFavorite(userId, movieId)
    }

    // ----- Writes -----

    /**
     * Add a favorite. Writes to Room immediately. If online, pushes to Firebase
     * and clears the pendingSync flag. If offline, leaves it in the queue.
     */
    suspend fun addFavorite(userId: String, movie: MovieModel) {
        val online = networkMonitor.isCurrentlyOnline()
        val entry = movie.toFavoriteEntry(
            userId = userId,
            pendingSync = !online,
            pendingDeletion = false
        )
        favoriteDao.upsert(entry)

        if (online) {
            try {
                pushSingleToFirebase(userId, entry)
                favoriteDao.clearPendingSync(userId, entry.movieId)
            } catch (e: Exception) {
                // Firebase write failed — keep in queue for later sync
                Log.w(TAG, "addFavorite: Firebase push failed, queued for sync", e)
                favoriteDao.upsert(entry.copy(pendingSync = true))
            }
        }
    }

    /**
     * Remove a favorite. Soft-deletes in Room (UI hides it instantly). If online,
     * deletes from Firebase and hard-deletes locally. If offline, leaves it queued.
     */
    suspend fun removeFavorite(userId: String, movieId: Int) {
        favoriteDao.markPendingDeletion(userId, movieId)

        if (networkMonitor.isCurrentlyOnline()) {
            try {
                deleteSingleFromFirebase(userId, movieId)
                favoriteDao.hardDelete(userId, movieId)
            } catch (e: Exception) {
                Log.w(TAG, "removeFavorite: Firebase delete failed, queued for sync", e)
                // Leave it in pendingDeletion state for the SyncManager
            }
        }
    }

    // ----- Sync operations -----

    /**
     * Push all locally-queued operations (adds + deletes) to Firebase.
     * Called by SyncManager when connectivity is restored.
     */
    suspend fun pushPendingToFirebase(userId: String) {
        if (!networkMonitor.isCurrentlyOnline()) return

        val pending = favoriteDao.getPendingSyncForUser(userId)
        for (entry in pending) {
            try {
                if (entry.pendingDeletion) {
                    deleteSingleFromFirebase(userId, entry.movieId)
                    favoriteDao.hardDelete(userId, entry.movieId)
                } else if (entry.pendingSync) {
                    pushSingleToFirebase(userId, entry)
                    favoriteDao.clearPendingSync(userId, entry.movieId)
                }
            } catch (e: Exception) {
                Log.w(TAG, "pushPendingToFirebase: failed for ${entry.movieId}", e)
                // Leave in queue; will retry on next sync trigger
            }
        }
    }

    /**
     * Pull the full favorites snapshot from Firebase and replace the local cache.
     * Pending local operations are preserved by [FavoriteDao.replaceAllForUser].
     */
    suspend fun pullFromFirebase(userId: String) {
        if (!networkMonitor.isCurrentlyOnline()) return

        try {
            val snapshot = readFirebaseSnapshot(userId)
            val entries = snapshotToEntries(userId, snapshot)
            favoriteDao.replaceAllForUser(userId, entries)
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
        firebaseRef = database.reference.child("favorites").child(userId)

        firebaseListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch {
                    try {
                        val entries = snapshotToEntries(userId, snapshot)
                        favoriteDao.replaceAllForUser(userId, entries)
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
     * Clear all locally-cached favorites for a user (used on sign-out).
     */
    suspend fun clearLocalForUser(userId: String) {
        favoriteDao.clearForUser(userId)
    }

    // ----- Firebase helpers -----

    private suspend fun pushSingleToFirebase(userId: String, entry: FavoriteEntry) =
        suspendCancellableCoroutine { cont ->
            database.reference
                .child("favorites")
                .child(userId)
                .child(entry.movieId.toString())
                .setValue(entry.toMovieModel())
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    private suspend fun deleteSingleFromFirebase(userId: String, movieId: Int) =
        suspendCancellableCoroutine { cont ->
            database.reference
                .child("favorites")
                .child(userId)
                .child(movieId.toString())
                .removeValue()
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    private suspend fun readFirebaseSnapshot(userId: String): DataSnapshot =
        suspendCancellableCoroutine { cont ->
            database.reference
                .child("favorites")
                .child(userId)
                .get()
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    private fun snapshotToEntries(userId: String, snapshot: DataSnapshot): List<FavoriteEntry> {
        val list = mutableListOf<FavoriteEntry>()
        if (!snapshot.exists()) return list
        for (child in snapshot.children) {
            val movie = child.getValue(MovieModel::class.java) ?: continue
            list.add(movie.toFavoriteEntry(userId, pendingSync = false, pendingDeletion = false))
        }
        return list
    }

    companion object {
        private const val TAG = "FavoritesRepository"
    }
}