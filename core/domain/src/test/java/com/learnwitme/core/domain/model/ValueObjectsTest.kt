package com.learnwitme.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ValueObjectsTest {

    @Test
    fun testTag_valid() {
        val tag = Tag("databases")
        assertEquals("databases", tag.name)
    }

    @Test
    fun testTag_invalid_blank() {
        try {
            Tag("  ")
            fail("Expected IllegalArgumentException for blank tag name")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("cannot be blank"))
        }
    }

    @Test
    fun testTag_invalid_whitespace() {
        try {
            Tag(" databases ")
            fail("Expected IllegalArgumentException for leading/trailing whitespace")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("cannot contain leading or trailing whitespace"))
        }
    }

    @Test
    fun testReference_valid() {
        val ref1 = Reference("SQL Guide")
        assertEquals("SQL Guide", ref1.title)
        assertNotNull(ref1)

        val ref2 = Reference("SQL Guide", "https://example.com")
        assertEquals("https://example.com", ref2.url)
    }

    @Test
    fun testReference_invalid_url() {
        try {
            Reference("SQL Guide", "ftp://example.com")
            fail("Expected IllegalArgumentException for invalid protocol")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("must be a valid http or https URL"))
        }
    }

    @Test
    fun testTopic_valid() {
        val topic = Topic("tp_databases", "Databases", "Concepts of DB")
        assertEquals("tp_databases", topic.code)
    }

    @Test
    fun testTopic_invalid_code() {
        try {
            Topic("databases", "Databases", "Concepts")
            fail("Expected IllegalArgumentException for invalid prefix")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("must start with 'tp_'"))
        }
    }

    @Test
    fun testSubTopic_valid() {
        val sub = SubTopic("sub_indexes", "tp_databases", "Indexes", "DB Indexing")
        assertEquals("sub_indexes", sub.code)
        assertEquals("tp_databases", sub.parentTopicCode)
    }

    @Test
    fun testSubTopic_invalid_code() {
        try {
            SubTopic("indexes", "tp_databases", "Indexes", "Description")
            fail("Expected IllegalArgumentException for invalid prefix")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("must start with 'sub_'"))
        }
    }

    @Test
    fun testCareerPath_valid() {
        val cp = CareerPath("cp_backend", "Backend Engineer", "Roadmap to backend")
        assertEquals("cp_backend", cp.code)
    }

    @Test
    fun testCareerPath_invalid_code() {
        try {
            CareerPath("backend", "Backend", "Desc")
            fail("Expected IllegalArgumentException for invalid prefix")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("must start with 'cp_'"))
        }
    }

    @Test
    fun testKnowledgeScore_valid() {
        val score = KnowledgeScore("tp_databases", 0.85, 123456789L)
        assertEquals(0.85, score.score, 0.0)
    }

    @Test
    fun testKnowledgeScore_invalid_range() {
        try {
            KnowledgeScore("tp_databases", 1.2, 123456789L)
            fail("Expected IllegalArgumentException for score > 1.0")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("must be between 0.0 and 1.0"))
        }
    }

    @Test
    fun testReviewSchedule_valid() {
        val schedule = ReviewSchedule(
            cardId = KnowledgeCardId("kc_1"),
            intervalDays = 4,
            nextReviewTimestamp = 1600000000L,
            easeFactor = 2.5,
            repetitions = 3
        )
        assertEquals(4, schedule.intervalDays)
        assertEquals(2.5, schedule.easeFactor, 0.0)
    }

    @Test
    fun testReviewSchedule_invalid_easeFactor() {
        try {
            ReviewSchedule(
                cardId = KnowledgeCardId("kc_1"),
                intervalDays = 4,
                nextReviewTimestamp = 1600000000L,
                easeFactor = 1.2,
                repetitions = 3
            )
            fail("Expected IllegalArgumentException for ease factor < 1.3")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("must be at least 1.3"))
        }
    }

    @Test
    fun testLearningProgress_valid() {
        val progress = LearningProgress(10, 5, 3, 1600000000L)
        assertEquals(10, progress.totalUnitsCompleted)
    }

    @Test
    fun testLearningProgress_invalid_negative() {
        try {
            LearningProgress(-1, 5, 3, 1600000000L)
            fail("Expected IllegalArgumentException for negative total units completed")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("cannot be negative"))
        }
    }

    @Test
    fun testLearningGoal_valid() {
        val goal = LearningGoal("Become senior backend", "cp_backend", DifficultyLevel.SENIOR)
        assertEquals("Become senior backend", goal.description)
    }

    @Test
    fun testRecommendation_valid() {
        val rec = Recommendation(KnowledgeCardId("kc_1"), 0.9, "Weak topic")
        assertEquals(0.9, rec.score, 0.0)
    }

    @Test
    fun testRecommendation_invalid_score() {
        try {
            Recommendation(KnowledgeCardId("kc_1"), -0.1, "Weak topic")
            fail("Expected IllegalArgumentException for negative score")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("must be between 0.0 and 1.0"))
        }
    }
}
