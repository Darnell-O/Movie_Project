package com.example.movie_project.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.movie_project.data.local.MovieLogDao
import com.example.movie_project.data.local.MovieLogEntry
import com.example.movie_project.util.NetworkMonitor
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
 * Unit tests for [MovieLogRepository] focused on the offline-first contract:
 *  - Offline writes go to Room only; Firebase is NOT called.
 *  - Offline removals soft-delete (markPendingDeletion) without hardDelete.
 *  - pushPendingToFirebase is a no-op when offline.
 *
 * Online Firebase paths are integration concerns and are exercised in
 * instrumented / manual testing.
 */
class MovieLogRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var dao: MovieLogDao
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var firebase: FirebaseDatabase
    private lateinit var repository: MovieLogRepository

    private val userId = "uid-1"
    private val entry = MovieLogEntry(
        entryId = "entry-1",
        movieTitle = "Inception",
        year = "2010",
        directedBy = "Christopher Nolan",
        starring = "Leonardo DiCaprio",
        rating = 5,
        inTheater = true
    )

    @Before
    fun setup() {
        dao = mock()
        networkMonitor = mock()
        firebase = mock()
        repository = MovieLogRepository(
            dao, networkMonitor, firebase, CoroutineScope(UnconfinedTestDispatcher())
        )
    }

    @Test
    fun addEntry_offline_queuesInRoomAndDoesNotClearPending() = runTest {
        whenever(networkMonitor.isCurrentlyOnline()).thenReturn(false)

        repository.addEntry(userId, entry)

        // Room is updated (with pendingSync = true under the hood)
        verify(dao).upsert(any<MovieLogEntry>())
        // Offline path must NOT clear pendingSync (Firebase wasn't reached)
        verify(dao, never()).clearPendingSync(any(), any())
    }

    @Test
    fun updateEntry_offline_queuesInRoomAndDoesNotClearPending() = runTest {
        whenever(networkMonitor.isCurrentlyOnline()).thenReturn(false)

        repository.updateEntry(userId, entry)

        verify(dao).upsert(any<MovieLogEntry>())
        verify(dao, never()).clearPendingSync(any(), any())
    }

    @Test
    fun deleteEntry_offline_marksPendingDeletionAndDoesNotHardDelete() = runTest {
        whenever(networkMonitor.isCurrentlyOnline()).thenReturn(false)

        repository.deleteEntry(userId, entry.entryId)

        verify(dao).markPendingDeletion(userId, entry.entryId)
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

    @Test
    fun pullFromFirebase_offline_isNoOp() = runTest {
        whenever(networkMonitor.isCurrentlyOnline()).thenReturn(false)

        repository.pullFromFirebase(userId)

        verify(dao, never()).replaceAllForUser(any(), any())
    }

    @Test
    fun observeEntries_delegatesToDao() {
        whenever(dao.getEntriesForUser(userId)).thenReturn(mock())

        val result = repository.observeEntries(userId)

        verify(dao).getEntriesForUser(userId)
    }

    @Test
    fun getEntryById_delegatesToDao() {
        whenever(dao.getEntryById(userId, entry.entryId)).thenReturn(mock())

        repository.getEntryById(userId, entry.entryId)

        verify(dao).getEntryById(userId, entry.entryId)
    }
}
