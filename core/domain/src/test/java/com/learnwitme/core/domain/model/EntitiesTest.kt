package com.learnwitme.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class EntitiesTest {

    @Test
    fun testKnowledgeCard_valid() {
        val card = KnowledgeCard(
            id = KnowledgeCardId("kc_sql_select"),
            packageId = KnowledgePackageId("pkg_1"),
            title = "SQL Select",
            summary = "Select query",
            whyItMatters = "Allows data retrieval",
            realWorldExample = "Loading dashboard data",
            commonMistakes = "SELECT *",
            whenNotToUse = "No DB queries needed",
            bestPractice = "Explicit columns",
            reference = listOf(Reference("SQL Docs")),
            officialDocumentationLink = "https://sql.com",
            difficulty = DifficultyLevel.BEGINNER,
            estimatedReadingTimeSeconds = 30,
            careerImpact = "High",
            tags = listOf(Tag("sql")),
            prerequisiteCardIds = emptyList(),
            relatedTopicCodes = emptyList()
        )
        assertEquals(KnowledgeCardId("kc_sql_select"), card.id)
        assertEquals("SQL Select", card.title)
    }

    @Test
    fun testKnowledgeCard_canonicalContentContract() {
        val card = KnowledgeCard(
            id = KnowledgeCardId("kc_contract_test"),
            packageId = KnowledgePackageId("pkg_canonical"),
            title = "Kotlin Coroutines Flow",
            summary = "Asynchronous cold stream",
            whyItMatters = "Handles reactive data streams safely",
            realWorldExample = "Fetching real-time updates from Room or Network",
            commonMistakes = "Collecting in wrong coroutine scope",
            whenNotToUse = "One-shot single value requests",
            bestPractice = "Use stateIn / shareIn with WhileSubscribed",
            reference = listOf(Reference("Kotlin Docs - Flow")),
            officialDocumentationLink = "https://kotlinlang.org/docs/flow.html",
            difficulty = DifficultyLevel.MID,
            estimatedReadingTimeSeconds = 180,
            careerImpact = "CRITICAL",
            tags = listOf(Tag("kotlin"), Tag("coroutines"), Tag("flow")),
            prerequisiteCardIds = listOf(KnowledgeCardId("kc_coroutines_basic")),
            relatedTopicCodes = listOf("tp_kotlin", "tp_android"),
            learningObjective = "Understand and apply Flow in Kotlin"
        )

        assertEquals("kc_contract_test", card.id.value)
        assertEquals("pkg_canonical", card.packageId.value)
        assertEquals("Kotlin Coroutines Flow", card.title)
        assertEquals("Asynchronous cold stream", card.summary)
        assertEquals(DifficultyLevel.MID, card.difficulty)
        assertEquals(180, card.estimatedReadingTimeSeconds)
        assertEquals("CRITICAL", card.careerImpact)
        assertEquals(3, card.tags.size)
        assertEquals("Understand and apply Flow in Kotlin", card.learningObjective)
        assertEquals("https://kotlinlang.org/docs/flow.html", card.officialDocumentationLink)
        assertEquals(1, card.references.size)
        assertEquals(1, card.prerequisiteCardIds.size)
        assertEquals(2, card.relatedTopicCodes.size)
    }

    @Test
    fun testKnowledgeCard_invalid_blankTitle() {
        try {
            KnowledgeCard(
                id = KnowledgeCardId("kc_sql_select"),
                packageId = KnowledgePackageId("pkg_1"),
                title = "  ",
                summary = "Select query",
                whyItMatters = "Allows data retrieval",
                realWorldExample = "Loading dashboard data",
                commonMistakes = "SELECT *",
                whenNotToUse = "No DB queries needed",
                bestPractice = "Explicit columns",
                reference = emptyList(),
                officialDocumentationLink = "",
                difficulty = DifficultyLevel.BEGINNER,
                estimatedReadingTimeSeconds = 30,
                careerImpact = "High",
                tags = emptyList(),
                prerequisiteCardIds = emptyList(),
                relatedTopicCodes = emptyList()
            )
            fail("Expected IllegalArgumentException for blank title")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("KnowledgeContent title cannot be blank"))
        }
    }

    @Test
    fun testKnowledgeCard_copy_invariantCheck() {
        val card = KnowledgeCard(
            id = KnowledgeCardId("kc_sql_select"),
            packageId = KnowledgePackageId("pkg_1"),
            title = "SQL Select",
            summary = "Select query",
            whyItMatters = "Allows data retrieval",
            realWorldExample = "Loading dashboard data",
            commonMistakes = "SELECT *",
            whenNotToUse = "No DB queries needed",
            bestPractice = "Explicit columns",
            reference = emptyList(),
            officialDocumentationLink = "",
            difficulty = DifficultyLevel.BEGINNER,
            estimatedReadingTimeSeconds = 30,
            careerImpact = "High",
            tags = emptyList(),
            prerequisiteCardIds = emptyList(),
            relatedTopicCodes = emptyList()
        )

        val updatedCard = card.copy(content = card.content.copy(title = "SQL SELECT Query"))
        assertEquals("SQL SELECT Query", updatedCard.content.title)

        try {
            card.copy(content = card.content.copy(title = ""))
            fail("Expected IllegalArgumentException on invalid copied state")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("KnowledgeContent title cannot be blank"))
        }
    }

    @Test
    fun testQuiz_valid() {
        val quiz = Quiz(
            id = QuizId("qz_1"),
            title = "SQL Basics",
            questions = listOf(
                QuizQuestion(
                    id = "q1",
                    questionText = "What does SQL stand for?",
                    options = listOf(
                        QuizOption("Structured Query Language", "Correct"),
                        QuizOption("Standard Query Language", "Incorrect")
                    ),
                    correctOptionIndex = 0
                )
            ),
            targetDifficulty = DifficultyLevel.BEGINNER
        )
        assertEquals(QuizId("qz_1"), quiz.id)
        assertEquals(1, quiz.questions.size)
    }

    @Test
    fun testQuiz_invalid_emptyQuestions() {
        try {
            Quiz(
                id = QuizId("qz_1"),
                title = "SQL Basics",
                questions = emptyList(),
                targetDifficulty = DifficultyLevel.BEGINNER
            )
            fail("Expected IllegalArgumentException for empty questions list")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("must contain at least one question"))
        }
    }

    @Test
    fun testQuizResult_valid() {
        val result = QuizResult(
            id = QuizResultId("qres_1"),
            quizId = QuizId("qz_1"),
            score = 1.0,
            answers = listOf(QuestionAnswer("q1", 0, true)),
            completedTimestamp = System.currentTimeMillis()
        )
        assertEquals(QuizResultId("qres_1"), result.id)
        assertEquals(1.0, result.score, 0.0)
    }

    @Test
    fun testQuizResult_invalid_scoreRange() {
        try {
            QuizResult(
                id = QuizResultId("qres_1"),
                quizId = QuizId("qz_1"),
                score = -0.5,
                answers = listOf(QuestionAnswer("q1", 0, true)),
                completedTimestamp = System.currentTimeMillis()
            )
            fail("Expected IllegalArgumentException for score < 0.0")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("score must be between 0.0 and 1.0"))
        }
    }

    @Test
    fun testLearningUnit_valid() {
        val unit = LearningUnit(
            id = LearningUnitId("lu_1"),
            title = "Intro to Select",
            cardId = KnowledgeCardId("kc_sql_select"),
            quizId = QuizId("qz_1"),
            estimatedMinutes = 10,
            topicCode = "tp_sql"
        )
        assertEquals(LearningUnitId("lu_1"), unit.id)
        assertEquals(KnowledgeCardId("kc_sql_select"), unit.cardId)
        assertEquals(QuizId("qz_1"), unit.quizId)
    }

    @Test
    fun testLearningUnit_invalid_estimatedMinutes() {
        try {
            LearningUnit(
                id = LearningUnitId("lu_1"),
                title = "Intro to Select",
                cardId = KnowledgeCardId("kc_sql_select"),
                quizId = null,
                estimatedMinutes = 0,
                topicCode = "tp_sql"
            )
            fail("Expected IllegalArgumentException for 0 estimated minutes")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Estimated minutes must be positive"))
        }
    }

    @Test
    fun testEntityEquality() {
        val card1 = KnowledgeCard(
            id = KnowledgeCardId("kc_1"),
            packageId = KnowledgePackageId("pkg_1"),
            title = "Title A",
            summary = "Summary", whyItMatters = "Matters", realWorldExample = "Example",
            commonMistakes = "Mistake", whenNotToUse = "Not to use", bestPractice = "Practice",
            reference = emptyList(), officialDocumentationLink = "",
            difficulty = DifficultyLevel.BEGINNER, estimatedReadingTimeSeconds = 10,
            careerImpact = "Impact", tags = emptyList(), prerequisiteCardIds = emptyList(), relatedTopicCodes = emptyList()
        )

        // Same ID, different title -> entities are considered equal by DDD definition (ID comparison)
        val card2 = card1.copy(content = card1.content.copy(title = "Title B"))
        assertEquals(card1.id, card2.id)

        val card3 = card1.copy(id = KnowledgeCardId("kc_2"))
        assertNotEquals(card1.id, card3.id)
    }
}
