package com.example.movie_project.data.sync

import android.util.Log
import com.example.movie_project.util.NetworkMonitor
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Generic offline-first sync engine shared by the favorites and movie-log
 * repositories. Owns all Room↔Firebase mechanics; the [store] supplies the
 * entity-specific bits (node name, key, encode/decode, DAO operations).
 *
 * Strategy:
 * - Reads always come from Room (offline-safe) — handled by the repositories.
 * - Writes go to Room first, then attempt Firebase if online; otherwise queued.
 * - A real-time listener mirrors remote changes into Room while online.
 *
 * Firebase = source of truth on conflict. Room = local cache + offline queue.
 */
class FirebaseSyncEngine<T, K>(
    private val store: SyncableStore<T, K>,
    private val database: FirebaseDatabase,
    private val networkMonitor: NetworkMonitor,
    private val scope: CoroutineScope,
) {

    private val _errorMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private var firebaseRef: DatabaseReference? = null
    private var firebaseListener: ValueEventListener? = null
    private var listeningForUserId: String? = null

    // ----- Writes -----

    /**
     * Upsert an entity to Room, then push to Firebase if online (clearing the
     * pendingSync flag). If offline or the push fails, it stays queued.
     */
    suspend fun upsertAndSync(userId: String, entity: T) {
        val online = networkMonitor.isCurrentlyOnline()
        val toStore = store.copyForSync(entity, pendingSync = !online)
        store.upsert(toStore)

        if (online) {
            try {
                pushSingle(userId, toStore)
                store.clearPendingSync(userId, store.keyOf(toStore))
            } catch (e: Exception) {
                Log.w(TAG, "upsertAndSync: push failed, queued for sync", e)
                store.upsert(store.copyForSync(toStore, pendingSync = true))
            }
        }
    }

    /**
     * Soft-delete in Room (UI hides it instantly), then delete from Firebase and
     * hard-delete locally if online. If offline or the delete fails, it stays queued.
     */
    suspend fun deleteAndSync(userId: String, key: K) {
        store.markPendingDeletion(userId, key)

        if (networkMonitor.isCurrentlyOnline()) {
            try {
                deleteSingle(userId, key)
                store.hardDelete(userId, key)
            } catch (e: Exception) {
                Log.w(TAG, "deleteAndSync: delete failed, queued for sync", e)
            }
        }
    }

    // ----- Sync operations -----

    /** Push all locally-queued operations (adds/updates + deletes) to Firebase. */
    suspend fun pushPending(userId: String) {
        if (!networkMonitor.isCurrentlyOnline()) return

        for (entity in store.getPendingSync(userId)) {
            val key = store.keyOf(entity)
            try {
                when {
                    store.isPendingDeletion(entity) -> {
                        deleteSingle(userId, key)
                        store.hardDelete(userId, key)
                    }
                    store.isPendingSync(entity) -> {
                        pushSingle(userId, entity)
                        store.clearPendingSync(userId, key)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "pushPending: failed for $key", e)
                // Leave in queue; will retry on next sync trigger
            }
        }
    }

    /** Pull the full snapshot from Firebase and replace the local cache. */
    suspend fun pull(userId: String) {
        if (!networkMonitor.isCurrentlyOnline()) return

        try {
            val snapshot = readSnapshot(userId)
            store.replaceAllForUser(userId, snapshotToEntities(userId, snapshot))
        } catch (e: Exception) {
            Log.e(TAG, "pull failed", e)
            e.localizedMessage?.let { _errorMessage.tryEmit(it) }
        }
    }

    /** Start the real-time Firebase listener, mirroring remote changes into Room. */
    fun startListener(userId: String) {
        if (listeningForUserId == userId && firebaseListener != null) return

        stopListener()
        listeningForUserId = userId
        firebaseRef = database.reference.child(store.firebaseNode).child(userId)

        firebaseListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch {
                    try {
                        store.replaceAllForUser(userId, snapshotToEntities(userId, snapshot))
                    } catch (e: Exception) {
                        Log.e(TAG, "listener: replaceAll failed", e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "listener cancelled: ${error.message}")
                _errorMessage.tryEmit(error.message)
            }
        }

        firebaseRef?.addValueEventListener(firebaseListener!!)
    }

    /** Stop the real-time listener (call on sign-out). */
    fun stopListener() {
        firebaseListener?.let { listener -> firebaseRef?.removeEventListener(listener) }
        firebaseListener = null
        firebaseRef = null
        listeningForUserId = null
    }

    /** Clear all locally-cached entries for a user (used on sign-out). */
    suspend fun clearLocal(userId: String) = store.clearForUser(userId)

    // ----- Firebase helpers -----

    private fun snapshotToEntities(userId: String, snapshot: DataSnapshot): List<T> {
        if (!snapshot.exists()) return emptyList()
        return snapshot.children.mapNotNull { store.decode(it, userId) }
    }

    private suspend fun pushSingle(userId: String, entity: T) =
        suspendCancellableCoroutine { cont ->
            database.reference
                .child(store.firebaseNode)
                .child(userId)
                .child(store.keyOf(entity).toString())
                .setValue(store.encodeForFirebase(entity))
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    private suspend fun deleteSingle(userId: String, key: K) =
        suspendCancellableCoroutine { cont ->
            database.reference
                .child(store.firebaseNode)
                .child(userId)
                .child(key.toString())
                .removeValue()
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    private suspend fun readSnapshot(userId: String): DataSnapshot =
        suspendCancellableCoroutine { cont ->
            database.reference
                .child(store.firebaseNode)
                .child(userId)
                .get()
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    companion object {
        private const val TAG = "FirebaseSyncEngine"
    }
}