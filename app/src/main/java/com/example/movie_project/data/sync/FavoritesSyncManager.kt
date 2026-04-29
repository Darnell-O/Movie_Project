package com.example.movie_project.data.sync

import android.util.Log
import androidx.lifecycle.Observer
import com.example.movie_project.data.repository.FavoritesRepository
import com.example.movie_project.util.NetworkMonitor
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Coordinates connectivity-driven sync between Room and Firebase.
 *
 * When connectivity returns:
 *   1) push all queued local operations to Firebase
 *   2) pull the latest Firebase snapshot into Room
 *
 * Lives at application scope.
 */
class FavoritesSyncManager(
    private val repository: FavoritesRepository,
    private val networkMonitor: NetworkMonitor,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val onlineObserver = Observer<Boolean> { isOnline ->
        if (isOnline == true) {
            triggerSync()
        }
    }

    /**
     * Begin observing connectivity and reacting to online transitions.
     * Call once from Application.onCreate.
     */
    fun start() {
        networkMonitor.isOnline.observeForever(onlineObserver)
    }

    /**
     * Stop observing connectivity. Call on app teardown if necessary.
     */
    fun stop() {
        networkMonitor.isOnline.removeObserver(onlineObserver)
    }

    /**
     * Manually trigger a full sync (push pending → pull from Firebase).
     */
    fun triggerSync() {
        val userId = auth.currentUser?.uid ?: return
        scope.launch {
            try {
                repository.pushPendingToFirebase(userId)
                repository.pullFromFirebase(userId)
            } catch (e: Exception) {
                Log.e(TAG, "Sync failed", e)
            }
        }
    }

    companion object {
        private const val TAG = "FavoritesSyncManager"
    }
}