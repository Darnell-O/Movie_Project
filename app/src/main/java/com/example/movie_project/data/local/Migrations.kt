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

/**
 * MIGRATION_2_3: Updates movie_log table for multi-user support and Firebase sync.
 * Changes:
 * - Replaces auto-generated id with composite key (userId + entryId)
 * - Adds sync metadata (pendingSync, pendingDeletion, updatedAt)
 * - Migrates existing entries to current user
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Get current user ID from Firebase Auth
        // Note: During migration, if no user is logged in, entries will be assigned to "unknown"
        // and can be reassigned later when user logs in
        val currentUserId = try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
        
        // Create new table with updated schema
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `movie_log_new` (
                `userId` TEXT NOT NULL,
                `entryId` TEXT NOT NULL,
                `movieTitle` TEXT NOT NULL DEFAULT '',
                `year` TEXT NOT NULL DEFAULT '',
                `dateWatched` TEXT NOT NULL DEFAULT '',
                `directedBy` TEXT NOT NULL DEFAULT '',
                `starring` TEXT NOT NULL DEFAULT '',
                `rating` INTEGER NOT NULL DEFAULT 0,
                `inTheater` INTEGER NOT NULL DEFAULT 0,
                `atHome` INTEGER NOT NULL DEFAULT 0,
                `firstWatch` INTEGER NOT NULL DEFAULT 0,
                `rewatch` INTEGER NOT NULL DEFAULT 0,
                `alone` INTEGER NOT NULL DEFAULT 0,
                `withSomeone` INTEGER NOT NULL DEFAULT 0,
                `notes` TEXT NOT NULL DEFAULT '',
                `dateAdded` INTEGER NOT NULL,
                `pendingSync` INTEGER NOT NULL DEFAULT 1,
                `pendingDeletion` INTEGER NOT NULL DEFAULT 0,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`userId`, `entryId`)
            )
            """.trimIndent()
        )
        
        // Create index on userId
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_movie_log_userId` ON `movie_log_new` (`userId`)"
        )
        
        // Migrate existing data to current user with UUID entryIds
        // Note: We use a SQL-based UUID generation for SQLite compatibility
        db.execSQL(
            """
            INSERT INTO `movie_log_new` (
                `userId`, `entryId`, `movieTitle`, `year`, `dateWatched`, `directedBy`,
                `starring`, `rating`, `inTheater`, `atHome`, `firstWatch`, `rewatch`,
                `alone`, `withSomeone`, `notes`, `dateAdded`, `pendingSync`,
                `pendingDeletion`, `updatedAt`
            )
            SELECT 
                '$currentUserId',
                lower(hex(randomblob(4)) || '-' || hex(randomblob(2)) || '-' || 
                     '4' || substr(hex(randomblob(2)), 2) || '-' || 
                     substr('89ab', abs(random()) % 4 + 1, 1) || substr(hex(randomblob(2)), 2) || '-' || 
                     hex(randomblob(6))),
                `movieTitle`, `year`, `dateWatched`, `directedBy`, `starring`, `rating`,
                `inTheater`, `atHome`, `firstWatch`, `rewatch`, `alone`, `withSomeone`,
                `notes`, `dateAdded`,
                1,
                0,
                `dateAdded`
            FROM `movie_log`
            """.trimIndent()
        )
        
        // Drop old table and rename new table
        db.execSQL("DROP TABLE `movie_log`")
        db.execSQL("ALTER TABLE `movie_log_new` RENAME TO `movie_log`")
    }
}
