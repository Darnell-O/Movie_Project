package com.example.movie_project.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.movie_project.data.local.MovieLogDao
import com.example.movie_project.data.local.MovieLogEntry
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for [MovieLogRepository].
 * Uses Mockito to mock [MovieLogDao] and verify correct delegation.
 */
class MovieLogRepositoryTest {

    private lateinit var dao: MovieLogDao
    private lateinit var repository: MovieLogRepository

    @Before
    fun setup() {
        dao = mock(MovieLogDao::class.java)

        // Stub getAllEntries() before constructing Repository (it's called in the property initializer)
        val fakeLiveData: LiveData<List<MovieLogEntry>> = MutableLiveData(emptyList())
        whenever(dao.getAllEntries()).thenReturn(fakeLiveData)

        repository = MovieLogRepository(dao)
    }

    @Test
    fun `allEntries returns LiveData from dao`() {
        // The repository's allEntries should be the same LiveData object returned by the DAO
        val result = repository.allEntries
        assertSame(dao.getAllEntries(), result)
    }

    @Test
    fun `getEntryById delegates to dao`() {
        val fakeEntry = MutableLiveData(MovieLogEntry(id = 1, movieTitle = "Test"))
        whenever(dao.getEntryById(1L)).thenReturn(fakeEntry)

        val result = repository.getEntryById(1L)

        verify(dao).getEntryById(1L)
        assertSame(fakeEntry, result)
    }

    @Test
    fun `insertEntry delegates to dao and returns id`() = runTest {
        val entry = MovieLogEntry(movieTitle = "Inception")
        whenever(dao.insertEntry(entry)).thenReturn(42L)

        val resultId = repository.insertEntry(entry)

        verify(dao).insertEntry(entry)
        assertEquals(42L, resultId)
    }

    @Test
    fun `updateEntry delegates to dao`() = runTest {
        val entry = MovieLogEntry(id = 1, movieTitle = "Updated Title")

        repository.updateEntry(entry)

        verify(dao).updateEntry(entry)
    }

    @Test
    fun `deleteEntry delegates to dao`() = runTest {
        val entry = MovieLogEntry(id = 1, movieTitle = "To Delete")

        repository.deleteEntry(entry)

        verify(dao).deleteEntry(entry)
    }
}
