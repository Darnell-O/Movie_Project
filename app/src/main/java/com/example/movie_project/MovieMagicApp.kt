package com.example.movie_project

import android.app.Application
import android.util.Log
import com.example.movie_project.data.sync.FavoritesSyncManager
import com.example.movie_project.data.sync.MovieLogSyncManager
import com.example.movie_project.util.NetworkMonitor
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class — bootstraps Hilt's dependency graph and kicks off
 * app-scoped singletons that need to start running as soon as the process
 * launches (connectivity monitoring + offline/online sync coordinators).
 *
 * All dependencies (Room, Retrofit, Firebase, repositories, sync managers)
 * are now provided by Hilt modules under [com.example.movie_project.di].
 */
@HiltAndroidApp
class MovieMagicApp : Application() {

    @Inject lateinit var networkMonitor: NetworkMonitor
    @Inject lateinit var favoritesSyncManager: FavoritesSyncManager
    @Inject lateinit var movieLogSyncManager: MovieLogSyncManager

    override fun onCreate() {
        // IMPORTANT: Firebase must be initialized and persistence configured BEFORE
        // Hilt builds its dependency graph (which happens when super.onCreate() is
        // called below, triggering @Inject field injection) — otherwise the
        // FirebaseDatabase instance becomes "frozen" and setPersistenceEnabled() throws.
        FirebaseApp.initializeApp(this)
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
            // Already enabled (e.g. in tests / app restart) — safe to ignore
            Log.w(TAG, "setPersistenceEnabled skipped: ${e.message}")
        }

        super.onCreate() // Hilt injects @Inject fields above during this call

        // Begin observing connectivity and trigger sync on reconnect
        networkMonitor.startMonitoring()
        favoritesSyncManager.start()
        movieLogSyncManager.start()
    }

    companion object {
        private const val TAG = "MovieMagicApp"
    }
}
