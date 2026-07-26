package com.learnwitme.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class KnowledgePackageTest {

    private fun createDummyCard(id: String): KnowledgeCard {
        return KnowledgeCard(
            id = KnowledgeCardId(id),
            title = "Title $id",
            summary = "Summary",
            whyItMatters = "Matters",
            realWorldExample = "Example",
            commonMistakes = "Mistake",
            whenNotToUse = "Not to use",
            bestPractice = "Practice",
            reference = emptyList(),
            officialDocumentationLink = "",
            difficulty = DifficultyLevel.BEGINNER,
            estimatedReadingTimeSeconds = 10,
            careerImpact = "Impact",
            tags = emptyList(),
            prerequisiteCardIds = emptyList(),
            relatedTopicCodes = emptyList()
        )
    }

    @Test
    fun testKnowledgePackage_creationAndInvariants() {
        val topic = Topic("tp_sql", "SQL", "SQL Basics")
        val pkg = KnowledgePackage(
            id = KnowledgePackageId("pkg_1"),
            title = "SQL Mastery",
            description = "Master database queries",
            version = 1,
            topics = listOf(topic)
        )

        assertEquals(KnowledgePackageId("pkg_1"), pkg.id)
        assertEquals(PackageStatus.DRAFT, pkg.status)
        assertEquals(1, pkg.topics.size)

        // Try creating with duplicate topics
        try {
            KnowledgePackage(
                id = KnowledgePackageId("pkg_1"),
                title = "SQL Mastery",
                description = "Master database queries",
                version = 1,
                topics = listOf(topic, topic)
            )
            fail("Expected IllegalArgumentException for duplicate topics")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("cannot contain duplicate Topics"))
        }

        // Try creating with duplicate cards
        val card = createDummyCard("kc_1")
        try {
            KnowledgePackage(
                id = KnowledgePackageId("pkg_1"),
                title = "SQL Mastery",
                description = "Master database queries",
                version = 1,
                cards = listOf(card, card)
            )
            fail("Expected IllegalArgumentException for duplicate cards")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("cannot contain duplicate KnowledgeCards"))
        }
    }

    @Test
    fun testKnowledgePackage_lifecycleTransitions() {
        val topic = Topic("tp_sql", "SQL", "SQL Basics")
        val card = createDummyCard("kc_1")
        var pkg = KnowledgePackage(
            id = KnowledgePackageId("pkg_1"),
            title = "SQL Mastery",
            description = "Master database queries",
            version = 1,
            topics = listOf(topic),
            cards = listOf(card)
        )

        // Draft -> Published
        pkg = pkg.publish()
        assertEquals(PackageStatus.PUBLISHED, pkg.status)

        // Published -> Deprecated
        pkg = pkg.deprecate()
        assertEquals(PackageStatus.DEPRECATED, pkg.status)

        // Deprecated -> Archived
        pkg = pkg.archive()
        assertEquals(PackageStatus.ARCHIVED, pkg.status)
    }

    @Test
    fun testKnowledgePackage_invalidTransitions() {
        var pkg = KnowledgePackage(
            id = KnowledgePackageId("pkg_1"),
            title = "SQL Mastery",
            description = "Master database queries",
            version = 1
        )

        // Try publishing without topics/cards
        try {
            pkg.publish()
            fail("Expected IllegalArgumentException for publishing empty package")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Cannot publish a package without topics"))
        }

        // Add topic and card to allow publishing
        pkg = pkg.addTopic(Topic("tp_sql", "SQL", "SQL Basics"))
            .addCard(createDummyCard("kc_1"))

        // Try deprecating while in DRAFT status
        try {
            pkg.deprecate()
            fail("Expected IllegalArgumentException for deprecating draft package")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Only PUBLISHED packages can be deprecated"))
        }
    }

    @Test
    fun testKnowledgePackage_archivedProtection() {
        val topic = Topic("tp_sql", "SQL", "SQL Basics")
        val card = createDummyCard("kc_1")
        var pkg = KnowledgePackage(
            id = KnowledgePackageId("pkg_1"),
            title = "SQL Mastery",
            description = "Master database queries",
            version = 1,
            topics = listOf(topic),
            cards = listOf(card)
        ).publish().archive()

        // Try modifying archived package
        try {
            pkg.addTopic(Topic("tp_nosql", "NoSQL", "NoSQL Basics"))
            fail("Expected IllegalArgumentException on modifying archived package")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Cannot modify an ARCHIVED package"))
        }

        try {
            pkg.addCard(createDummyCard("kc_2"))
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Cannot modify an ARCHIVED package"))
        }
    }

    @Test
    fun testKnowledgePackage_equality() {
        val pkg1 = KnowledgePackage(
            id = KnowledgePackageId("pkg_1"),
            title = "Package 1",
            description = "Desc",
            version = 1
        )

        val pkg2 = pkg1.copy(title = "Package 1 updated")
        assertEquals(pkg1.id, pkg2.id)

        val pkg3 = pkg1.copy(id = KnowledgePackageId("pkg_2"))
        assertNotEquals(pkg1.id, pkg3.id)
    }
}
