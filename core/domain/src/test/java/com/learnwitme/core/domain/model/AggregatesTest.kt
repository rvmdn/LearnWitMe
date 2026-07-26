package com.learnwitme.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AggregatesTest {

    @Test
    fun testLearningRoadmap_creationAndInvariants() {
        val goal = LearningGoal("Learn SQL", "cp_backend", DifficultyLevel.MID)
        val unit1 = LearningUnit(LearningUnitId("lu_1"), "Select", KnowledgeCardId("kc_1"), null, 10, "tp_sql")
        val unit2 = LearningUnit(LearningUnitId("lu_2"), "Where", KnowledgeCardId("kc_2"), null, 15, "tp_sql")

        val roadmap = LearningRoadmap(
            id = LearningRoadmapId("rm_1"),
            goal = goal,
            units = listOf(unit1, unit2)
        )

        assertEquals(LearningRoadmapId("rm_1"), roadmap.id)
        assertEquals(2, roadmap.units.size)
        assertEquals(RoadmapStatus.ACTIVE, roadmap.status)

        // Try creating with duplicate units
        try {
            LearningRoadmap(
                id = LearningRoadmapId("rm_1"),
                goal = goal,
                units = listOf(unit1, unit1)
            )
            fail("Expected IllegalArgumentException for duplicate learning units")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("cannot contain duplicate Learning Units"))
        }
    }

    @Test
    fun testLearningRoadmap_completeUnitAndLifecycle() {
        val goal = LearningGoal("Learn SQL", "cp_backend", DifficultyLevel.MID)
        val unit1 = LearningUnit(LearningUnitId("lu_1"), "Select", KnowledgeCardId("kc_1"), null, 10, "tp_sql")
        val unit2 = LearningUnit(LearningUnitId("lu_2"), "Where", KnowledgeCardId("kc_2"), null, 15, "tp_sql")

        var roadmap = LearningRoadmap(
            id = LearningRoadmapId("rm_1"),
            goal = goal,
            units = listOf(unit1, unit2)
        )

        assertEquals(unit1, roadmap.getNextPendingUnit())

        // Complete first unit
        roadmap = roadmap.completeUnit(LearningUnitId("lu_1"))
        assertTrue(roadmap.completedUnitIds.contains(LearningUnitId("lu_1")))
        assertEquals(RoadmapStatus.ACTIVE, roadmap.status)
        assertEquals(unit2, roadmap.getNextPendingUnit())

        // Complete second unit (completes the roadmap)
        roadmap = roadmap.completeUnit(LearningUnitId("lu_2"))
        assertEquals(RoadmapStatus.COMPLETED, roadmap.status)
        assertNull(roadmap.getNextPendingUnit())
    }

    @Test
    fun testLearningRoadmap_completeUnit_errors() {
        val goal = LearningGoal("Learn SQL", "cp_backend", DifficultyLevel.MID)
        val unit1 = LearningUnit(LearningUnitId("lu_1"), "Select", KnowledgeCardId("kc_1"), null, 10, "tp_sql")

        var roadmap = LearningRoadmap(
            id = LearningRoadmapId("rm_1"),
            goal = goal,
            units = listOf(unit1)
        )

        // 1. Completing a unit that does not exist in roadmap
        try {
            roadmap.completeUnit(LearningUnitId("lu_99"))
            fail("Expected IllegalArgumentException for non-existent unit")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("does not belong to this roadmap"))
        }

        // Archive the roadmap
        roadmap = roadmap.archive()
        assertEquals(RoadmapStatus.ARCHIVED, roadmap.status)

        // 2. Completing unit in ARCHIVED roadmap
        try {
            roadmap.completeUnit(LearningUnitId("lu_1"))
            fail("Expected IllegalArgumentException for non-active roadmap")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Cannot complete units in a roadmap that is not ACTIVE"))
        }
    }

    @Test
    fun testUserProfile_creationAndInvariants() {
        val careerPath = CareerPath("cp_backend", "Backend", "Backend road")
        val preference = LearningPreference(listOf("Kotlin"), 30)
        val progress = LearningProgress(0, 0, 0, System.currentTimeMillis())

        val profile = UserProfile(
            id = UserId("usr_1"),
            careerPath = careerPath,
            preference = preference,
            currentLevel = DifficultyLevel.BEGINNER,
            targetLevel = DifficultyLevel.SENIOR,
            progress = progress
        )

        assertEquals(UserId("usr_1"), profile.id)

        // Try creating with current level higher than target level
        try {
            UserProfile(
                id = UserId("usr_1"),
                careerPath = careerPath,
                preference = preference,
                currentLevel = DifficultyLevel.SENIOR,
                targetLevel = DifficultyLevel.BEGINNER,
                progress = progress
            )
            fail("Expected IllegalArgumentException when current difficulty > target")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("cannot be higher than target level"))
        }
    }

    @Test
    fun testUserProfile_stateTransitions() {
        val careerPath = CareerPath("cp_backend", "Backend", "Backend road")
        val preference = LearningPreference(listOf("Kotlin"), 30)
        val progress = LearningProgress(0, 0, 0, System.currentTimeMillis())

        var profile = UserProfile(
            id = UserId("usr_1"),
            careerPath = careerPath,
            preference = preference,
            currentLevel = DifficultyLevel.BEGINNER,
            targetLevel = DifficultyLevel.SENIOR,
            progress = progress
        )

        // Update preferences
        val newPref = LearningPreference(listOf("SQL", "Security"), 45)
        profile = profile.updatePreference(newPref)
        assertEquals(newPref, profile.preference)

        // Update career goal
        val newCareer = CareerPath("cp_android", "Android", "Android road")
        profile = profile.updateCareerGoal(newCareer, DifficultyLevel.MID, DifficultyLevel.SENIOR)
        assertEquals(newCareer, profile.careerPath)
        assertEquals(DifficultyLevel.MID, profile.currentLevel)

        // Invalid update career goal
        try {
            profile.updateCareerGoal(newCareer, DifficultyLevel.SENIOR, DifficultyLevel.BEGINNER)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("cannot be higher than new target level"))
        }
    }

    @Test
    fun testAggregateEquality() {
        val careerPath = CareerPath("cp_backend", "Backend", "Backend road")
        val preference = LearningPreference(listOf("Kotlin"), 30)
        val progress = LearningProgress(0, 0, 0, System.currentTimeMillis())

        val profile1 = UserProfile(
            id = UserId("usr_1"),
            careerPath = careerPath,
            preference = preference,
            currentLevel = DifficultyLevel.BEGINNER,
            targetLevel = DifficultyLevel.SENIOR,
            progress = progress
        )

        val profile2 = profile1.copy(currentLevel = DifficultyLevel.MID)
        // Aggregate roots are equal if their IDs are equal
        assertEquals(profile1.id, profile2.id)

        val profile3 = profile1.copy(id = UserId("usr_2"))
        assertNotEquals(profile1.id, profile3.id)
    }
}
