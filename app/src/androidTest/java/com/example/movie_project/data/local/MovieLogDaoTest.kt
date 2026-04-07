package com.example.movie_project.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [MovieLogDao].
 * Uses an in-memory Room database so tests run fast and don't affect real data.
 * Must run on a device or emulator (androidTest).
 */
@RunWith(AndroidJUnit4::class)
class MovieLogDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: MovieLogDatabase
    private lateinit var dao: MovieLogDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MovieLogDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = database.movieLogDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveEntry() = runTest {
        val entry = MovieLogEntry(
            movieTitle = "Inception",
            year = "2010",
            directedBy = "Christopher Nolan",
            starring = "Leonardo DiCaprio",
            rating = 5,
            inTheater = true,
            dateAdded = 1000L
        )

        val insertedId = dao.insertEntry(entry)
        assertTrue(insertedId > 0)

        val retrieved = dao.getEntryById(insertedId).getOrAwaitValue()
        assertNotNull(retrieved)
        assertEquals("Inception", retrieved.movieTitle)
        assertEquals("2010", retrieved.year)
        assertEquals("Christopher Nolan", retrieved.directedBy)
        assertEquals("Leonardo DiCaprio", retrieved.starring)
        assertEquals(5, retrieved.rating)
        assertTrue(retrieved.inTheater)
    }

    @Test
    fun getAllEntries_orderedByDateDesc() = runTest {
        val oldest = MovieLogEntry(movieTitle = "Oldest", dateAdded = 1000L)
        val middle = MovieLogEntry(movieTitle = "Middle", dateAdded = 2000L)
        val newest = MovieLogEntry(movieTitle = "Newest", dateAdded = 3000L)

        dao.insertEntry(oldest)
        dao.insertEntry(middle)
        dao.insertEntry(newest)

        val entries = dao.getAllEntries().getOrAwaitValue()

        assertEquals(3, entries.size)
        assertEquals("Newest", entries[0].movieTitle)
        assertEquals("Middle", entries[1].movieTitle)
        assertEquals("Oldest", entries[2].movieTitle)
    }

    @Test
    fun updateEntry_modifiesExistingRow() = runTest {
        val entry = MovieLogEntry(movieTitle = "Original Title", dateAdded = 1000L)
        val id = dao.insertEntry(entry)

        val updated = entry.copy(id = id, movieTitle = "Updated Title")
        dao.updateEntry(updated)

        val retrieved = dao.getEntryById(id).getOrAwaitValue()
        assertEquals("Updated Title", retrieved.movieTitle)
    }

    @Test
    fun deleteEntry_removesFromDatabase() = runTest {
        val entry = MovieLogEntry(movieTitle = "To Delete", dateAdded = 1000L)
        val id = dao.insertEntry(entry)

        val inserted = dao.getEntryById(id).getOrAwaitValue()
        assertNotNull(inserted)

        dao.deleteEntry(inserted)

        val allEntries = dao.getAllEntries().getOrAwaitValue()
        assertTrue(allEntries.isEmpty())
    }

    @Test
    fun getEntryById_returnsCorrectEntry() = runTest {
        val entry1 = MovieLogEntry(movieTitle = "Movie A", dateAdded = 1000L)
        val entry2 = MovieLogEntry(movieTitle = "Movie B", dateAdded = 2000L)
        val entry3 = MovieLogEntry(movieTitle = "Movie C", dateAdded = 3000L)

        dao.insertEntry(entry1)
        val id2 = dao.insertEntry(entry2)
        dao.insertEntry(entry3)

        val retrieved = dao.getEntryById(id2).getOrAwaitValue()
        assertNotNull(retrieved)
        assertEquals("Movie B", retrieved.movieTitle)
    }
}
