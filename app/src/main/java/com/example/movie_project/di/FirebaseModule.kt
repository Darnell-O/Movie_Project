package com.example.movie_project.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides Firebase Auth/Database singletons for Hilt.
 *
 * NOTE: Persistence (setPersistenceEnabled) is configured once in
 * [com.example.movie_project.MovieMagicApp.onCreate] BEFORE Hilt's dependency
 * graph is built (i.e. before `super.onCreate()` triggers field injection),
 * so by the time this module is asked for a [FirebaseDatabase] instance it is
 * already safely configured.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()
}
