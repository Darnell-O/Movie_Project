package com.example.movie_project.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.movie_project.data.local.FavoriteDao
import com.example.movie_project.data.local.FavoriteEntry
import com.example.movie_project.models.MovieModel
import com.example.movie_project.util.NetworkMonitor
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for [FavoritesRepository] focused on the offline-first contract:
 *  - Offline writes go to Room only; Firebase is NOT called.
 *  - Offline removals soft-delete (markPendingDeletion) without hardDelete.
 *  - pushPendingToFirebase is a no-op when offline.
 *
 * Online Firebase paths are integration concerns and are exercised in
 * instrumented / manual testing.
 */
class FavoritesRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var dao: FavoriteDao
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var firebase: FirebaseDatabase
    private lateinit var repository: FavoritesRepository

    private val userId = "uid-1"
    private val movie = MovieModel(
        id = 42,
        title = "Inception",
        overview = "A heist of dreams",
        poster_path = "/poster.jpg",
        voteAverage = 8.8f,
        release_date = "2010-07-16"
    )

    @Before
    fun setup() {
        dao = mock()
        networkMonitor = mock()
        firebase = mock()
        repository = FavoritesRepository(dao, networkMonitor, firebase)
    }

    @Test
    fun addFavorite_offline_queuesInRoomAndDoesNotClearPending() = runTest {
        whenever(networkMonitor.isCurrentlyOnline()).thenReturn(false)

        repository.addFavorite(userId, movie)

        // Room is updated (with pendingSync = true under the hood)
        verify(dao).upsert(any<FavoriteEntry>())
        // Offline path must NOT clear pendingSync (Firebase wasn't reached)
        verify(dao, never()).clearPendingSync(any(), any())
    }

    @Test
    fun removeFavorite_offline_marksPendingDeletionAndDoesNotHardDelete() = runTest {
        whenever(networkMonitor.isCurrentlyOnline()).thenReturn(false)

        repository.removeFavorite(userId, movie.id)

        verify(dao).markPendingDeletion(any(), any(), any())
        verify(dao, never()).hardDelete(any(), any())
    }

    @Test
    fun pushPendingToFirebase_offline_isNoOp() = runTest {
        whenever(networkMonitor.isCurrentlyOnline()).thenReturn(false)

        repository.pushPendingToFirebase(userId)

        // No queue read, no DAO mutations
        verify(dao, never()).getPendingSyncForUser(any())
        verify(dao, never()).clearPendingSync(any(), any())
        verify(dao, never()).hardDelete(any(), any())
    }
}