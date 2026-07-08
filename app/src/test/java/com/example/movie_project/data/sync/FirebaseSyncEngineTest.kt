package com.example.movie_project.data.sync

import com.example.movie_project.util.NetworkMonitor
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Unit tests for [FirebaseSyncEngine]'s offline contract — the store-orchestration
 * logic that used to be duplicated across the two repositories:
 *  - Offline writes queue in the store without clearing pendingSync.
 *  - Offline removals soft-delete without hard-deleting.
 *  - pushPending / pull are no-ops when offline.
 *
 * Online Firebase paths are integration concerns (manual / instrumented testing).
 */
class FirebaseSyncEngineTest {

    private data class TestEntity(
        val id: Int,
        val pendingSync: Boolean = false,
        val pendingDeletion: Boolean = false
    )

    /** Hand-rolled fake that records the store interactions we assert on. */
    private class FakeStore : SyncableStore<TestEntity, Int> {
        val upserted = mutableListOf<TestEntity>()
        var clearPendingCalled = false
        var markDeletionCalled = false
        var hardDeleteCalled = false
        var replaceAllCalled = false
        var getPendingCalled = false

        override val firebaseNode = "test"
        override fun keyOf(entity: TestEntity) = entity.id
        override fun isPendingSync(entity: TestEntity) = entity.pendingSync
        override fun isPendingDeletion(entity: TestEntity) = entity.pendingDeletion
        override fun copyForSync(entity: TestEntity, pendingSync: Boolean) =
            entity.copy(pendingSync = pendingSync, pendingDeletion = false)

        override fun decode(snapshot: DataSnapshot, userId: String): TestEntity? = null
        override fun encodeForFirebase(entity: TestEntity): Any = entity

        override suspend fun getPendingSync(userId: String): List<TestEntity> {
            getPendingCalled = true
            return emptyList()
        }
        override suspend fun upsert(entity: TestEntity) { upserted.add(entity) }
        override suspend fun clearPendingSync(userId: String, key: Int) { clearPendingCalled = true }
        override suspend fun markPendingDeletion(userId: String, key: Int) { markDeletionCalled = true }
        override suspend fun hardDelete(userId: String, key: Int) { hardDeleteCalled = true }
        override suspend fun clearForUser(userId: String) {}
        override suspend fun replaceAllForUser(userId: String, entities: List<TestEntity>) {
            replaceAllCalled = true
        }
    }

    private lateinit var store: FakeStore
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var database: FirebaseDatabase
    private lateinit var engine: FirebaseSyncEngine<TestEntity, Int>

    private val userId = "uid-1"

    @Before
    fun setup() {
        store = FakeStore()
        networkMonitor = mock()
        database = mock()
        engine = FirebaseSyncEngine(store, database, networkMonitor, CoroutineScope(UnconfinedTestDispatcher()))
    }

    @Test
    fun upsertAndSync_offline_queuesWithPendingSyncAndDoesNotClear() = runTest {
        whenever(networkMonitor.isCurrentlyOnline()).thenReturn(false)

        engine.upsertAndSync(userId, TestEntity(id = 1))

        assertEquals(1, store.upserted.size)
        assertTrue("offline write should be queued (pendingSync=true)", store.upserted[0].pendingSync)
        assertFalse("pendingSync must not be cleared while offline", store.clearPendingCalled)
    }

    @Test
    fun deleteAndSync_offline_softDeletesWithoutHardDelete() = runTest {
        whenever(networkMonitor.isCurrentlyOnline()).thenReturn(false)

        engine.deleteAndSync(userId, 1)

        assertTrue("offline delete should soft-delete", store.markDeletionCalled)
        assertFalse("must not hard-delete while offline", store.hardDeleteCalled)
    }

    @Test
    fun pushPending_offline_isNoOp() = runTest {
        whenever(networkMonitor.isCurrentlyOnline()).thenReturn(false)

        engine.pushPending(userId)

        assertFalse("pushPending must not read the queue while offline", store.getPendingCalled)
    }

    @Test
    fun pull_offline_isNoOp() = runTest {
        whenever(networkMonitor.isCurrentlyOnline()).thenReturn(false)

        engine.pull(userId)

        assertFalse("pull must not replace local cache while offline", store.replaceAllCalled)
    }
}