package com.example.movie_project.data.sync

import android.util.Log
import androidx.lifecycle.Observer
import com.example.movie_project.data.repository.AuthRepository
import com.example.movie_project.di.ApplicationScope
import com.example.movie_project.util.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coordinates connectivity-driven sync between Room and Firebase for every
 * [Syncable] store (favorites, movie log, …) collected via Hilt multibindings.
 *
 * When connectivity returns, each store pushes its queued local operations and
 * pulls the latest remote snapshot. Lives at application scope.
 */
@Singleton
class SyncManager @Inject constructor(
    private val syncables: Set<@JvmSuppressWildcards Syncable>,
    private val networkMonitor: NetworkMonitor,
    private val authRepository: AuthRepository,
    @ApplicationScope private val scope: CoroutineScope,
) {

    private val onlineObserver = Observer<Boolean> { isOnline ->
        if (isOnline == true) triggerSync()
    }

    /** Begin observing connectivity. Call once from Application.onCreate. */
    fun start() {
        networkMonitor.isOnline.observeForever(onlineObserver)
    }

    /** Stop observing connectivity. Call on app teardown if necessary. */
    fun stop() {
        networkMonitor.isOnline.removeObserver(onlineObserver)
    }

    /** Manually trigger a full sync (push pending → pull) for every store. */
    fun triggerSync() {
        val userId = authRepository.currentUserId ?: return
        scope.launch {
            syncables.forEach { syncable ->
                try {
                    syncable.sync(userId)
                } catch (e: Exception) {
                    Log.e(TAG, "sync failed for ${syncable::class.simpleName}", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "SyncManager"
    }
}