package com.example.movie_project.data.sync

import com.google.firebase.database.DataSnapshot

/**
 * Captures everything that differs between the two offline-first stores
 * (favorites vs movie log) so the shared sync mechanics can live in a single
 * [FirebaseSyncEngine].
 *
 * @param T the Room entity type
 * @param K the entity's key type (Int movieId for favorites, String entryId for the log)
 */
interface SyncableStore<T, K> {

    /** Top-level Firebase node under which entries live (`node/{userId}/{key}`). */
    val firebaseNode: String

    fun keyOf(entity: T): K
    fun isPendingSync(entity: T): Boolean
    fun isPendingDeletion(entity: T): Boolean

    /** Returns a copy with [pendingSync] set and pendingDeletion cleared — used to (re)queue writes. */
    fun copyForSync(entity: T, pendingSync: Boolean): T

    /** Decodes a Firebase child snapshot into an entity, or null if it can't be read. */
    fun decode(snapshot: DataSnapshot, userId: String): T?

    /** The value written to Firebase for this entity (may be a remote DTO). */
    fun encodeForFirebase(entity: T): Any

    suspend fun getPendingSync(userId: String): List<T>
    suspend fun upsert(entity: T)
    suspend fun clearPendingSync(userId: String, key: K)
    suspend fun markPendingDeletion(userId: String, key: K)
    suspend fun hardDelete(userId: String, key: K)
    suspend fun clearForUser(userId: String)
    suspend fun replaceAllForUser(userId: String, entities: List<T>)
}