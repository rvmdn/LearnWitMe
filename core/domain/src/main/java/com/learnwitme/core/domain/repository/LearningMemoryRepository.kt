package com.learnwitme.core.domain.repository

import com.learnwitme.core.domain.model.LearningMemory
import com.learnwitme.core.domain.model.LearningMemoryId
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.memory.LearningMemorySnapshot

/**
 * Responsibility: Repository contract for managing the [LearningMemory] aggregate root
 * and its associated [LearningMemorySnapshot] history.
 *
 * This is a pure domain interface — no implementation lives here.
 * Infrastructure layer provides the concrete implementation via
 * dependency inversion (Clean Architecture §2.3).
 *
 * Architecture Invariant:
 * - AI never calls this repository directly.
 * - Only the Memory Engine writes to LearningMemory through this contract.
 * - Recommendation Engine, Quiz Engine, and Feed Engine read from it.
 *
 * Sprint 9 expansion: Adds snapshot persistence and retrieval operations.
 */
interface LearningMemoryRepository {

    // ── Core Aggregate Operations ─────────────────────────────────────

    /**
     * Retrieves the [LearningMemory] for a given memory ID.
     * Returns null if no memory record exists for the given ID.
     */
    suspend fun getMemory(id: LearningMemoryId): LearningMemory?

    /**
     * Retrieves the [LearningMemory] for a given user.
     * Returns null if no memory record exists for this user.
     */
    suspend fun getMemoryByUserId(userId: UserId): LearningMemory?

    /**
     * Persists (inserts or updates) a [LearningMemory] aggregate.
     * The entire aggregate state is replaced atomically.
     */
    suspend fun saveMemory(memory: LearningMemory)

    /**
     * Deletes the [LearningMemory] and all associated snapshots for a given user.
     * Used for account deletion and data purge workflows.
     */
    suspend fun deleteMemoryByUserId(userId: UserId)

    // ── Snapshot Operations ───────────────────────────────────────────

    /**
     * Persists an immutable [LearningMemorySnapshot].
     * Snapshots are never updated after creation — this is insert-only.
     */
    suspend fun saveSnapshot(snapshot: LearningMemorySnapshot)

    /**
     * Retrieves a specific snapshot by its unique snapshot ID.
     * Returns null if no snapshot exists for the given ID.
     */
    suspend fun getSnapshot(snapshotId: String): LearningMemorySnapshot?

    /**
     * Retrieves all snapshots for a given [LearningMemory], ordered by
     * [LearningMemorySnapshot.capturedEpochMs] ascending (oldest first).
     *
     * Returns an empty list if no snapshots exist.
     */
    suspend fun getSnapshotsForMemory(memoryId: LearningMemoryId): List<LearningMemorySnapshot>

    /**
     * Retrieves the most recent snapshot for a given [LearningMemory].
     * Returns null if no snapshots have been taken yet.
     */
    suspend fun getLatestSnapshot(memoryId: LearningMemoryId): LearningMemorySnapshot?

    /**
     * Retrieves snapshots for a given memory within a time range (inclusive).
     * Ordered by [LearningMemorySnapshot.capturedEpochMs] ascending.
     *
     * @param memoryId       The memory aggregate to query snapshots for.
     * @param fromEpochMs    Start of the time range (epoch ms, inclusive).
     * @param toEpochMs      End of the time range (epoch ms, inclusive).
     */
    suspend fun getSnapshotsInRange(
        memoryId: LearningMemoryId,
        fromEpochMs: Long,
        toEpochMs: Long
    ): List<LearningMemorySnapshot>

    /**
     * Deletes all snapshots for a given memory older than [beforeEpochMs].
     * Used for storage management and data retention policy enforcement.
     *
     * @param memoryId      The memory aggregate to prune snapshots for.
     * @param beforeEpochMs Epoch timestamp; snapshots older than this are deleted.
     */
    suspend fun pruneSnapshotsBefore(memoryId: LearningMemoryId, beforeEpochMs: Long)
}
