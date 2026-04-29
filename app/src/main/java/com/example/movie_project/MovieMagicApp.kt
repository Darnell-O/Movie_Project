package com.example.movie_project

import android.app.Application
import android.util.Log
import com.example.movie_project.data.local.AppDatabase
import com.example.movie_project.data.repository.FavoritesRepository
import com.example.movie_project.data.sync.FavoritesSyncManager
import com.example.movie_project.util.NetworkMonitor
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

/**
 * Application class — owns app-scoped singletons:
 * - Room database (AppDatabase)
 * - NetworkMonitor (connectivity)
 * - FavoritesRepository (single source of truth)
 * - FavoritesSyncManager (online-trigger sync)
 *
 * Use [MovieMagicApp.from] from any Context to access these.
 */
class MovieMagicApp : Application() {

    lateinit var networkMonitor: NetworkMonitor
        private set

    lateinit var favoritesRepository: FavoritesRepository
        private set

    lateinit var favoritesSyncManager: FavoritesSyncManager
        private set

    override fun onCreate() {
        super.onCreate()

        // IMPORTANT: Firebase must be initialized and persistence configured BEFORE
        // anything else touches FirebaseDatabase.getInstance() — otherwise the
        // instance becomes "frozen" and setPersistenceEnabled() throws.
        FirebaseApp.initializeApp(this)
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
            // Already enabled (e.g. in tests / app restart) — safe to ignore
            Log.w("MovieMagicApp", "setPersistenceEnabled skipped: ${e.message}")
        }

        val db = AppDatabase.getDatabase(this)
        networkMonitor = NetworkMonitor(this)
        favoritesRepository = FavoritesRepository(
            favoriteDao = db.favoriteDao(),
            networkMonitor = networkMonitor
        )
        favoritesSyncManager = FavoritesSyncManager(
            repository = favoritesRepository,
            networkMonitor = networkMonitor
        )

        // Begin observing connectivity and trigger sync on reconnect
        networkMonitor.startMonitoring()
        favoritesSyncManager.start()
    }

    companion object {
        fun from(context: android.content.Context): MovieMagicApp =
            context.applicationContext as MovieMagicApp
    }
}