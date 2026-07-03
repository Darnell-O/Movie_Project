package com.example.movie_project.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Unified Room Database for the app.
 * Consolidates MovieLog and Favorites tables.
 * Version 2 adds the "favorites" table via MIGRATION_1_2.
 *
 * The instance is constructed and provided as a singleton by
 * [com.example.movie_project.di.DatabaseModule] via Hilt.
 */
@Database(
    entities = [MovieLogEntry::class, FavoriteEntry::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun movieLogDao(): MovieLogDao
    abstract fun favoriteDao(): FavoriteDao
}
