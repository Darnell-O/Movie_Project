package com.example.movie_project.data.sync

/**
 * A store that can reconcile its local (Room) state with the remote (Firebase)
 * backend for a given user. Implemented by the offline-first repositories and
 * driven collectively by [SyncManager] via Hilt multibindings.
 */
interface Syncable {
    /** Push queued local operations, then pull the latest remote snapshot for [userId]. */
    suspend fun sync(userId: String)
}