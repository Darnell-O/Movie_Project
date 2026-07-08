package com.example.movie_project.data.repository

import com.example.movie_project.data.local.FavoriteDao
import com.example.movie_project.data.local.FavoriteEntry
import com.example.movie_project.data.local.toFavoriteEntry
import com.example.movie_project.data.local.toMovieModel
import com.example.movie_project.data.sync.FirebaseSyncEngine
import com.example.movie_project.data.sync.Syncable
import com.example.movie_project.data.sync.SyncableStore
import com.example.movie_project.di.ApplicationScope
import com.example.movie_project.models.domain.MovieModel
import com.example.movie_project.util.NetworkMonitor
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for favorites. Reads come from Room; writes and Firebase
 * sync are delegated to a shared [FirebaseSyncEngine]. The [SyncableStore] below
 * supplies the favorites-specific bits (node, key, encode/decode, DAO ops).
 */
@Singleton
class FavoritesRepository @Inject constructor(
    private val favoriteDao: FavoriteDao,
    networkMonitor: NetworkMonitor,
    database: FirebaseDatabase,
    @ApplicationScope scope: CoroutineScope
) : Syncable {

    private val store = object : SyncableStore<FavoriteEntry, Int> {
        override val firebaseNode = "favorites"
        override fun keyOf(entity: FavoriteEntry) = entity.movieId
        override fun isPendingSync(entity: FavoriteEntry) = entity.pendingSync
        override fun isPendingDeletion(entity: FavoriteEntry) = entity.pendingDeletion
        override fun copyForSync(entity: FavoriteEntry, pendingSync: Boolean) =
            entity.copy(pendingSync = pendingSync, pendingDeletion = false)

        override fun decode(snapshot: DataSnapshot, userId: String): FavoriteEntry? =
            snapshot.getValue(FavoriteRemoteDto::class.java)?.toFavoriteEntry(userId)

        override fun encodeForFirebase(entity: FavoriteEntry): Any = entity.toRemoteDto()

        override suspend fun getPendingSync(userId: String) =
            favoriteDao.getPendingSyncForUser(userId)
        override suspend fun upsert(entity: FavoriteEntry) = favoriteDao.upsert(entity)
        override suspend fun clearPendingSync(userId: String, key: Int) =
            favoriteDao.clearPendingSync(userId, key)
        override suspend fun markPendingDeletion(userId: String, key: Int) =
            favoriteDao.markPendingDeletion(userId, key)
        override suspend fun hardDelete(userId: String, key: Int) =
            favoriteDao.hardDelete(userId, key)
        override suspend fun clearForUser(userId: String) = favoriteDao.clearForUser(userId)
        override suspend fun replaceAllForUser(userId: String, entities: List<FavoriteEntry>) =
            favoriteDao.replaceAllForUser(userId, entities)
    }

    private val engine = FirebaseSyncEngine(store, database, networkMonitor, scope)

    val errorMessage: SharedFlow<String> = engine.errorMessage

    // ----- Reads -----

    fun observeFavorites(userId: String): Flow<List<MovieModel>> =
        favoriteDao.getFavoritesForUser(userId).map { entries -> entries.map { it.toMovieModel() } }

    fun isFavorite(userId: String, movieId: Int): Flow<Boolean> =
        favoriteDao.isFavorite(userId, movieId)

    // ----- Writes -----

    suspend fun addFavorite(userId: String, movie: MovieModel) =
        engine.upsertAndSync(userId, movie.toFavoriteEntry(userId))

    suspend fun removeFavorite(userId: String, movieId: Int) =
        engine.deleteAndSync(userId, movieId)

    // ----- Sync operations -----

    suspend fun pushPendingToFirebase(userId: String) = engine.pushPending(userId)
    suspend fun pullFromFirebase(userId: String) = engine.pull(userId)
    fun startFirebaseListener(userId: String) = engine.startListener(userId)
    fun stopFirebaseListener() = engine.stopListener()
    suspend fun clearLocalForUser(userId: String) = engine.clearLocal(userId)

    override suspend fun sync(userId: String) {
        pushPendingToFirebase(userId)
        pullFromFirebase(userId)
    }
}