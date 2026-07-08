package com.example.movie_project.data.repository

import android.util.Log
import com.example.movie_project.data.local.MovieLogDao
import com.example.movie_project.data.local.MovieLogEntry
import com.example.movie_project.data.sync.FirebaseSyncEngine
import com.example.movie_project.data.sync.Syncable
import com.example.movie_project.data.sync.SyncableStore
import com.example.movie_project.di.ApplicationScope
import com.example.movie_project.util.NetworkMonitor
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for the Movie Log. Reads come from Room; writes and
 * Firebase sync are delegated to a shared [FirebaseSyncEngine]. The [SyncableStore]
 * below supplies the movie-log-specific bits (node, key, encode/decode, DAO ops).
 */
@Singleton
class MovieLogRepository @Inject constructor(
    private val movieLogDao: MovieLogDao,
    networkMonitor: NetworkMonitor,
    database: FirebaseDatabase,
    @ApplicationScope private val scope: CoroutineScope
) : Syncable {

    private val store = object : SyncableStore<MovieLogEntry, String> {
        override val firebaseNode = "movieLog"
        override fun keyOf(entity: MovieLogEntry) = entity.entryId
        override fun isPendingSync(entity: MovieLogEntry) = entity.pendingSync
        override fun isPendingDeletion(entity: MovieLogEntry) = entity.pendingDeletion
        override fun copyForSync(entity: MovieLogEntry, pendingSync: Boolean) =
            entity.copy(pendingSync = pendingSync, pendingDeletion = false)

        override fun decode(snapshot: DataSnapshot, userId: String): MovieLogEntry? =
            snapshot.getValue(MovieLogEntry::class.java)
                ?.copy(userId = userId, pendingSync = false, pendingDeletion = false)

        override fun encodeForFirebase(entity: MovieLogEntry): Any = entity

        override suspend fun getPendingSync(userId: String) =
            movieLogDao.getPendingSyncForUser(userId)
        override suspend fun upsert(entity: MovieLogEntry) = movieLogDao.upsert(entity)
        override suspend fun clearPendingSync(userId: String, key: String) =
            movieLogDao.clearPendingSync(userId, key)
        override suspend fun markPendingDeletion(userId: String, key: String) =
            movieLogDao.markPendingDeletion(userId, key)
        override suspend fun hardDelete(userId: String, key: String) =
            movieLogDao.hardDelete(userId, key)
        override suspend fun clearForUser(userId: String) = movieLogDao.clearForUser(userId)
        override suspend fun replaceAllForUser(userId: String, entities: List<MovieLogEntry>) =
            movieLogDao.replaceAllForUser(userId, entities)
    }

    private val engine = FirebaseSyncEngine(store, database, networkMonitor, scope)

    val errorMessage: SharedFlow<String> = engine.errorMessage

    // ----- Reads -----

    fun observeEntries(userId: String): Flow<List<MovieLogEntry>> =
        movieLogDao.getEntriesForUser(userId)

    fun getEntryById(userId: String, entryId: String): Flow<MovieLogEntry?> =
        movieLogDao.getEntryById(userId, entryId)

    // ----- Writes -----

    suspend fun addEntry(userId: String, entry: MovieLogEntry) =
        engine.upsertAndSync(userId, entry.copy(userId = userId, updatedAt = System.currentTimeMillis()))

    suspend fun updateEntry(userId: String, entry: MovieLogEntry) =
        engine.upsertAndSync(userId, entry.copy(userId = userId, updatedAt = System.currentTimeMillis()))

    suspend fun deleteEntry(userId: String, entryId: String) =
        engine.deleteAndSync(userId, entryId)

    // ----- Sync operations -----

    suspend fun pushPendingToFirebase(userId: String) = engine.pushPending(userId)
    suspend fun pullFromFirebase(userId: String) = engine.pull(userId)

    fun startFirebaseListener(userId: String) {
        // Adopt any entries left under the "unknown" sentinel by an offline MIGRATION_2_3.
        scope.launch {
            try {
                movieLogDao.reassignOrphanedEntries(userId)
            } catch (e: Exception) {
                Log.w(TAG, "reassignOrphanedEntries failed", e)
            }
        }
        engine.startListener(userId)
    }

    fun stopFirebaseListener() = engine.stopListener()
    suspend fun clearLocalForUser(userId: String) = engine.clearLocal(userId)

    override suspend fun sync(userId: String) {
        pushPendingToFirebase(userId)
        pullFromFirebase(userId)
    }

    companion object {
        private const val TAG = "MovieLogRepository"
    }
}