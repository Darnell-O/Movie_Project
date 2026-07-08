package com.example.movie_project.di

import com.example.movie_project.data.repository.FavoritesRepository
import com.example.movie_project.data.repository.MovieLogRepository
import com.example.movie_project.data.sync.Syncable
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

/**
 * Collects every [Syncable] repository into a Set so [SyncManager] can drive them
 * uniformly, without knowing which concrete stores exist.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    @IntoSet
    abstract fun bindFavoritesSyncable(impl: FavoritesRepository): Syncable

    @Binds
    @IntoSet
    abstract fun bindMovieLogSyncable(impl: MovieLogRepository): Syncable
}