package com.learnwitme.core.domain.model

enum class RoadmapStatus {
    ACTIVE, COMPLETED, ARCHIVED
}

/**
 * Responsibility: Serves as the Aggregate Root that coordinates a structured learning path
 * (collection of LearningUnits) tailored to a specific LearningGoal.
 */
data class LearningRoadmap(
    override val id: LearningRoadmapId,
    val goal: LearningGoal,
    val units: List<LearningUnit>,
    val status: RoadmapStatus = RoadmapStatus.ACTIVE,
    val completedUnitIds: Set<LearningUnitId> = emptySet()
) : AggregateRoot<LearningRoadmapId> {

    init {
        // Validate roadmap: No duplicate units
        val unitIds = units.map { it.id }
        require(unitIds.size == unitIds.toSet().size) { "Roadmap cannot contain duplicate Learning Units" }
    }

    /**
     * Completes a Learning Unit within the roadmap, returning a new immutable roadmap instance
     * with the updated progress.
     */
    fun completeUnit(unitId: LearningUnitId): LearningRoadmap {
        val unit = units.find { it.id == unitId }
            ?: throw IllegalArgumentException("Learning Unit $unitId does not belong to this roadmap")

        if (completedUnitIds.contains(unitId)) {
            return this // Already completed
        }

        // Verify that the roadmap is active
        require(status == RoadmapStatus.ACTIVE) { "Cannot complete units in a roadmap that is not ACTIVE" }

        val newCompletedIds = completedUnitIds + unitId
        val allUnitsCompleted = units.all { newCompletedIds.contains(it.id) }
        val newStatus = if (allUnitsCompleted) RoadmapStatus.COMPLETED else status

        return this.copy(
            completedUnitIds = newCompletedIds,
            status = newStatus
        )
    }

    /**
     * Determines the next pending Learning Unit in the roadmap sequence.
     */
    fun getNextPendingUnit(): LearningUnit? {
        if (status != RoadmapStatus.ACTIVE) return null
        return units.firstOrNull { !completedUnitIds.contains(it.id) }
    }

    /**
     * Archives this roadmap.
     */
    fun archive(): LearningRoadmap {
        return this.copy(status = RoadmapStatus.ARCHIVED)
    }
}
