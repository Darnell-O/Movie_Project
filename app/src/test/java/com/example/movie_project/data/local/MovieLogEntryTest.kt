package com.example.movie_project.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [MovieLogEntry] data class.
 * Verifies default values and custom value retention.
 */
class MovieLogEntryTest {

    @Test
    fun `default values are correct when no arguments provided`() {
        val entry = MovieLogEntry()

        assertEquals("", entry.userId)
        // entryId defaults to a randomly generated UUID string
        assertTrue(entry.entryId.isNotBlank())
        assertEquals("", entry.movieTitle)
        assertEquals("", entry.year)
        assertEquals("", entry.dateWatched)
        assertEquals("", entry.directedBy)
        assertEquals("", entry.starring)
        assertEquals(0, entry.rating)
        assertFalse(entry.inTheater)
        assertFalse(entry.atHome)
        assertFalse(entry.firstWatch)
        assertFalse(entry.rewatch)
        assertFalse(entry.alone)
        assertFalse(entry.withSomeone)
        assertEquals("", entry.notes)
        // dateAdded should be a positive timestamp (not 0)
        assertTrue(entry.dateAdded > 0)
        assertFalse(entry.pendingSync)
        assertFalse(entry.pendingDeletion)
        assertTrue(entry.updatedAt > 0)
    }

    @Test
    fun `custom values are retained when provided`() {
        val entry = MovieLogEntry(
            userId = "user-1",
            entryId = "entry-42",
            movieTitle = "Inception",
            year = "2010",
            dateWatched = "July 16, 2010",
            directedBy = "Christopher Nolan",
            starring = "Leonardo DiCaprio",
            rating = 5,
            inTheater = true,
            atHome = false,
            firstWatch = true,
            rewatch = false,
            alone = false,
            withSomeone = true,
            notes = "Mind-bending masterpiece",
            dateAdded = 1234567890L,
            pendingSync = true,
            pendingDeletion = false,
            updatedAt = 987654321L
        )

        assertEquals("user-1", entry.userId)
        assertEquals("entry-42", entry.entryId)
        assertEquals("Inception", entry.movieTitle)
        assertEquals("2010", entry.year)
        assertEquals("July 16, 2010", entry.dateWatched)
        assertEquals("Christopher Nolan", entry.directedBy)
        assertEquals("Leonardo DiCaprio", entry.starring)
        assertEquals(5, entry.rating)
        assertTrue(entry.inTheater)
        assertFalse(entry.atHome)
        assertTrue(entry.firstWatch)
        assertFalse(entry.rewatch)
        assertFalse(entry.alone)
        assertTrue(entry.withSomeone)
        assertEquals("Mind-bending masterpiece", entry.notes)
        assertEquals(1234567890L, entry.dateAdded)
        assertTrue(entry.pendingSync)
        assertFalse(entry.pendingDeletion)
        assertEquals(987654321L, entry.updatedAt)
    }
}
