package com.example.movie_project.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room Database for Movie Log feature.
 * Singleton pattern ensures only one instance of the database is created.
 */
@Database(entities = [MovieLogEntry::class], version = 1, exportSchema = false)
abstract class MovieLogDatabase : RoomDatabase() {

    abstract fun movieLogDao(): MovieLogDao

    companion object {
        @Volatile
        private var INSTANCE: MovieLogDatabase? = null

        fun getDatabase(context: Context): MovieLogDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MovieLogDatabase::class.java,
                    "movie_log_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
