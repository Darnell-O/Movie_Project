package com.example.movie_project.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database migrations.
 * MIGRATION_1_2: Adds the "favorites" table for offline-first favorites caching.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `favorites` (
                `userId` TEXT NOT NULL,
                `movieId` INTEGER NOT NULL,
                `title` TEXT,
                `overview` TEXT,
                `posterPath` TEXT,
                `voteAverage` REAL,
                `releaseDate` TEXT,
                `poster` TEXT NOT NULL DEFAULT '',
                `pendingSync` INTEGER NOT NULL DEFAULT 0,
                `pendingDeletion` INTEGER NOT NULL DEFAULT 0,
                `updatedAt` INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(`userId`, `movieId`)
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_favorites_userId` ON `favorites` (`userId`)"
        )
    }
}