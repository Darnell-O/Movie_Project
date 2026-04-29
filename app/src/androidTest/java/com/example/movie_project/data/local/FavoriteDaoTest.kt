package com.example.movie_project.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [FavoriteDao].
 * Uses an in-memory Room database to verify CRUD, per-user isolation,
 * sync queue queries, and the replaceAllForUser transaction.
 */
@RunWith(AndroidJUnit4::class)
class FavoriteDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var dao: FavoriteDao

    private val userA = "userA"
    private val userB = "userB"

    private fun fav(
        userId: String,
        movieId: Int,
        title: String = "Movie $movieId",
        pendingSync: Boolean = false,
        pendingDeletion: Boolean = false,
        updatedAt: Long = movieId * 1000L
    ) = FavoriteEntry(
        userId = userId,
        movieId = movieId,
        title = title,
        overview = "",
        posterPath = "",
        voteAverage = 0f,
        releaseDate = "",
        poster = "",
        pendingSync = pendingSync,
        pendingDeletion = pendingDeletion,
        updatedAt = updatedAt
    )

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.favoriteDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun upsertAndObserve_returnsOnlyForRequestedUser() = runTest {
        dao.upsert(fav(userA, 1, "A1"))
        dao.upsert(fav(userA, 2, "A2"))
        dao.upsert(fav(userB, 3, "B1"))

        val aList = dao.getFavoritesForUser(userA).getOrAwaitValue()
        val bList = dao.getFavoritesForUser(userB).getOrAwaitValue()

        assertEquals(2, aList.size)
        assertEquals(1, bList.size)
        assertTrue(aList.all { it.userId == userA })
        assertEquals("B1", bList[0].title)
    }

    @Test
    fun favoritesOrderedByUpdatedAtDesc() = runTest {
        dao.upsert(fav(userA, 1, "Old", updatedAt = 1000L))
        dao.upsert(fav(userA, 2, "Newest", updatedAt = 3000L))
        dao.upsert(fav(userA, 3, "Middle", updatedAt = 2000L))

        val list = dao.getFavoritesForUser(userA).getOrAwaitValue()
        assertEquals(listOf("Newest", "Middle", "Old"), list.map { it.title })
    }

    @Test
    fun pendingDeletion_excludedFromObserve() = runTest {
        dao.upsert(fav(userA, 1, "Visible"))
        dao.upsert(fav(userA, 2, "Hidden"))
        dao.markPendingDeletion(userA, 2)

        val list = dao.getFavoritesForUser(userA).getOrAwaitValue()
        assertEquals(1, list.size)
        assertEquals("Visible", list[0].title)
    }

    @Test
    fun isFavorite_reflectsPresenceAndPendingDeletion() = runTest {
        dao.upsert(fav(userA, 1))
        assertTrue(dao.isFavorite(userA, 1).getOrAwaitValue())
        assertFalse(dao.isFavorite(userA, 999).getOrAwaitValue())

        dao.markPendingDeletion(userA, 1)
        assertFalse(dao.isFavorite(userA, 1).getOrAwaitValue())
    }

    @Test
    fun pendingSyncQueue_returnsBothAddsAndDeletes() = runTest {
        dao.upsert(fav(userA, 1, pendingSync = true))    // offline add
        dao.upsert(fav(userA, 2))                         // already synced
        dao.upsert(fav(userA, 3))
        dao.markPendingDeletion(userA, 3)                 // offline delete

        val pending = dao.getPendingSyncForUser(userA)
        assertEquals(2, pending.size)
        val ids = pending.map { it.movieId }.toSet()
        assertTrue(ids.contains(1))
        assertTrue(ids.contains(3))
    }

    @Test
    fun clearPendingSync_clearsFlagOnly() = runTest {
        dao.upsert(fav(userA, 1, pendingSync = true))
        dao.clearPendingSync(userA, 1)

        val list = dao.getFavoritesForUser(userA).getOrAwaitValue()
        assertEquals(1, list.size)
        assertFalse(list[0].pendingSync)
    }

    @Test
    fun hardDelete_removesEntry() = runTest {
        dao.upsert(fav(userA, 1))
        dao.hardDelete(userA, 1)
        assertTrue(dao.getFavoritesForUser(userA).getOrAwaitValue().isEmpty())
    }

    @Test
    fun clearForUser_onlyAffectsThatUser() = runTest {
        dao.upsert(fav(userA, 1))
        dao.upsert(fav(userB, 2))
        dao.clearForUser(userA)

        assertTrue(dao.getFavoritesForUser(userA).getOrAwaitValue().isEmpty())
        assertEquals(1, dao.getFavoritesForUser(userB).getOrAwaitValue().size)
    }

    @Test
    fun replaceAllForUser_preservesPendingOfflineAdd() = runTest {
        // Existing synced favorite from earlier
        dao.upsert(fav(userA, 1, "Old"))
        // User added a new favorite while offline (not yet on Firebase)
        dao.upsert(fav(userA, 99, "OfflineAdd", pendingSync = true))

        // Firebase snapshot only contains the old one
        val fresh = listOf(fav(userA, 1, "Old (server)"))
        dao.replaceAllForUser(userA, fresh)

        val list = dao.getFavoritesForUser(userA).getOrAwaitValue()
        val ids = list.map { it.movieId }.toSet()
        assertTrue("Offline add should be preserved", ids.contains(99))
        assertTrue("Server data should be present", ids.contains(1))
    }

    @Test
    fun replaceAllForUser_preservesPendingOfflineDelete() = runTest {
        dao.upsert(fav(userA, 1, "ToDelete"))
        dao.markPendingDeletion(userA, 1)

        // Firebase still has it because the delete hasn't synced yet
        val fresh = listOf(fav(userA, 1, "ToDelete (server)"))
        dao.replaceAllForUser(userA, fresh)

        // Should be hidden (pendingDeletion still set)
        val visible = dao.getFavoritesForUser(userA).getOrAwaitValue()
        assertTrue("Pending delete should remain hidden", visible.isEmpty())

        val pending = dao.getPendingSyncForUser(userA)
        assertEquals(1, pending.size)
        assertTrue(pending[0].pendingDeletion)
    }
}