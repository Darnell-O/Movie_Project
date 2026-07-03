package com.example.movie_project.di

import android.content.Context
import androidx.room.Room
import com.example.movie_project.data.local.AppDatabase
import com.example.movie_project.data.local.FavoriteDao
import com.example.movie_project.data.local.MIGRATION_1_2
import com.example.movie_project.data.local.MIGRATION_2_3
import com.example.movie_project.data.local.MovieLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides the Room database and DAOs for Hilt.
 * Replaces the former [AppDatabase.getDatabase] manual singleton accessor.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "movie_log_database" // keep same DB name to preserve existing data
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    @Provides
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao = database.favoriteDao()

    @Provides
    fun provideMovieLogDao(database: AppDatabase): MovieLogDao = database.movieLogDao()
}
