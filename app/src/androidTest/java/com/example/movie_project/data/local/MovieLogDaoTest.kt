package com.example.movie_project.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [MovieLogDao].
 * Uses an in-memory Room database to verify CRUD, per-user isolation,
 * sync queue queries, and the replaceAllForUser transaction.
 */
@RunWith(AndroidJUnit4::class)
class MovieLogDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var dao: MovieLogDao

    private val userA = "userA"
    private val userB = "userB"

    private fun entry(
        userId: String,
        entryId: String,
        movieTitle: String = "Movie $entryId",
        dateAdded: Long = 1000L,
        pendingSync: Boolean = false,
        pendingDeletion: Boolean = false,
        updatedAt: Long = dateAdded
    ) = MovieLogEntry(
        userId = userId,
        entryId = entryId,
        movieTitle = movieTitle,
        year = "2010",
        directedBy = "Director",
        starring = "Star",
        rating = 5,
        inTheater = true,
        dateAdded = dateAdded,
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

        dao = database.movieLogDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun upsertAndObserve_returnsOnlyForRequestedUser() = runTest {
        dao.upsert(entry(userA, "a1", "A1"))
        dao.upsert(entry(userA, "a2", "A2"))
        dao.upsert(entry(userB, "b1", "B1"))

        val aList = dao.getEntriesForUser(userA).first()
        val bList = dao.getEntriesForUser(userB).first()

        assertEquals(2, aList.size)
        assertEquals(1, bList.size)
        assertTrue(aList.all { it.userId == userA })
        assertEquals("B1", bList[0].movieTitle)
    }

    @Test
    fun entriesOrderedByDateAddedDesc() = runTest {
        dao.upsert(entry(userA, "old", "Oldest", dateAdded = 1000L))
        dao.upsert(entry(userA, "mid", "Middle", dateAdded = 2000L))
        dao.upsert(entry(userA, "new", "Newest", dateAdded = 3000L))

        val entries = dao.getEntriesForUser(userA).first()

        assertEquals(3, entries.size)
        assertEquals("Newest", entries[0].movieTitle)
        assertEquals("Middle", entries[1].movieTitle)
        assertEquals("Oldest", entries[2].movieTitle)
    }

    @Test
    fun getEntryById_returnsCorrectEntry() = runTest {
        dao.upsert(entry(userA, "e1", "Movie A"))
        dao.upsert(entry(userA, "e2", "Movie B"))
        dao.upsert(entry(userA, "e3", "Movie C"))

        val retrieved = dao.getEntryById(userA, "e2").first()
        assertTrue(retrieved != null)
        assertEquals("Movie B", retrieved?.movieTitle)
    }

    @Test
    fun upsert_updatesExistingEntry() = runTest {
        dao.upsert(entry(userA, "e1", "Original Title"))
        dao.upsert(entry(userA, "e1", "Updated Title"))

        val retrieved = dao.getEntryById(userA, "e1").first()
        assertEquals("Updated Title", retrieved?.movieTitle)

        // Still only one row for that user/entryId combo (REPLACE, not duplicate)
        val all = dao.getEntriesForUser(userA).first()
        assertEquals(1, all.size)
    }

    @Test
    fun markPendingDeletion_excludedFromObserveAndGetById() = runTest {
        dao.upsert(entry(userA, "visible", "Visible"))
        dao.upsert(entry(userA, "hidden", "Hidden"))
        dao.markPendingDeletion(userA, "hidden")

        val list = dao.getEntriesForUser(userA).first()
        assertEquals(1, list.size)
        assertEquals("Visible", list[0].movieTitle)

        val hidden = dao.getEntryById(userA, "hidden").first()
        assertNull(hidden)
    }

    @Test
    fun hardDelete_removesFromDatabase() = runTest {
        dao.upsert(entry(userA, "e1", "To Delete"))
        dao.hardDelete(userA, "e1")

        val allEntries = dao.getEntriesForUser(userA).first()
        assertTrue(allEntries.isEmpty())
    }

    @Test
    fun clearForUser_onlyAffectsThatUser() = runTest {
        dao.upsert(entry(userA, "a1"))
        dao.upsert(entry(userB, "b1"))
        dao.clearForUser(userA)

        assertTrue(dao.getEntriesForUser(userA).first().isEmpty())
        assertEquals(1, dao.getEntriesForUser(userB).first().size)
    }

    @Test
    fun pendingSyncQueue_returnsBothAddsAndDeletes() = runTest {
        dao.upsert(entry(userA, "e1", pendingSync = true)) // offline add
        dao.upsert(entry(userA, "e2")) // already synced
        dao.upsert(entry(userA, "e3"))
        dao.markPendingDeletion(userA, "e3") // offline delete

        val pending = dao.getPendingSyncForUser(userA)
        assertEquals(2, pending.size)
        val ids = pending.map { it.entryId }.toSet()
        assertTrue(ids.contains("e1"))
        assertTrue(ids.contains("e3"))
    }

    @Test
    fun clearPendingSync_clearsFlagOnly() = runTest {
        dao.upsert(entry(userA, "e1", pendingSync = true))
        dao.clearPendingSync(userA, "e1")

        val list = dao.getEntriesForUser(userA).first()
        assertEquals(1, list.size)
        assertFalse(list[0].pendingSync)
    }

    @Test
    fun replaceAllForUser_preservesPendingOfflineAdd() = runTest {
        // Existing synced entry from earlier
        dao.upsert(entry(userA, "e1", "Old"))
        // User added a new entry while offline (not yet on Firebase)
        dao.upsert(entry(userA, "offline", "OfflineAdd", pendingSync = true))

        // Firebase snapshot only contains the old one
        val fresh = listOf(entry(userA, "e1", "Old (server)"))
        dao.replaceAllForUser(userA, fresh)

        val list = dao.getEntriesForUser(userA).first()
        val ids = list.map { it.entryId }.toSet()
        assertTrue("Offline add should be preserved", ids.contains("offline"))
        assertTrue("Server data should be present", ids.contains("e1"))
    }

    @Test
    fun replaceAllForUser_preservesPendingOfflineDelete() = runTest {
        dao.upsert(entry(userA, "e1", "ToDelete"))
        dao.markPendingDeletion(userA, "e1")

        // Firebase still has it because the delete hasn't synced yet
        val fresh = listOf(entry(userA, "e1", "ToDelete (server)"))
        dao.replaceAllForUser(userA, fresh)

        // Should be hidden (pendingDeletion still set)
        val visible = dao.getEntriesForUser(userA).first()
        assertTrue("Pending delete should remain hidden", visible.isEmpty())

        val pending = dao.getPendingSyncForUser(userA)
        assertEquals(1, pending.size)
        assertTrue(pending[0].pendingDeletion)
    }
}
