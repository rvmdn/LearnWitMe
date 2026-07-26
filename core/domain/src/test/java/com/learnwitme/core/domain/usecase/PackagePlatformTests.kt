package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.*
import com.learnwitme.core.domain.service.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class FakeQuizRepoForImport : QuizRepository {
    val savedQuizzes = mutableListOf<Quiz>()
    override suspend fun getQuiz(id: QuizId): Quiz? = savedQuizzes.find { it.id == id }
    override suspend fun saveQuiz(quiz: Quiz) { savedQuizzes.add(quiz) }
    override suspend fun getQuizResult(id: QuizResultId): QuizResult? = null
    override suspend fun saveQuizResult(result: QuizResult) {}
    override suspend fun getQuizResultsByUserId(userId: UserId): List<QuizResult> = emptyList()
}

class FakeKnowledgeRepoForImport : KnowledgeRepository {
    val savedCards = mutableListOf<KnowledgeCard>()
    override suspend fun findCard(id: KnowledgeCardId): KnowledgeCard? = savedCards.find { it.id == id }
    override suspend fun getCard(id: KnowledgeCardId): KnowledgeCard? = findCard(id)
    override suspend fun saveCard(card: KnowledgeCard) { savedCards.add(card) }
    override suspend fun deleteCard(id: KnowledgeCardId) {}
    override suspend fun getCardsByTopic(topicCode: String): List<KnowledgeCard> = emptyList()
    override suspend fun searchBySkills(skills: List<Skill>): List<KnowledgeCard> = emptyList()
    override suspend fun findPackage(packageId: KnowledgePackageId): KnowledgePackage? = null
    override suspend fun importPackage(pkg: KnowledgePackage) {}
    override suspend fun publishPackage(packageId: KnowledgePackageId) {}
}

class PackagePlatformTests {

    private val packageRepo = FakePackageRepo()
    private val knowledgeRepo = FakeKnowledgeRepoForImport()
    private val quizRepo = FakeQuizRepoForImport()
    private val validator = PackageValidator(packageRepo, knowledgeRepo)
    private val importer = PackageImporter(packageRepo, knowledgeRepo, quizRepo, validator)
    private val workflow = ImportPackageWorkflow(importer)

    private val packageId = KnowledgePackageId("pkg1")
    private val cardId1 = KnowledgeCardId("c1")
    private val cardId2 = KnowledgeCardId("c2")

    private val card1 = KnowledgeCard(
        cardId1, "Card 1", "Summary", "Why", "Example", "Mistakes", "WhenNot", "Best", 
        emptyList(), "http://doc", DifficultyLevel.BEGINNER, 120, "Impact", 
        emptyList(), emptyList(), listOf("tp_sql")
    )
    private val card2 = KnowledgeCard(
        cardId2, "Card 2", "Summary", "Why", "Example", "Mistakes", "WhenNot", "Best", 
        emptyList(), "http://doc", DifficultyLevel.BEGINNER, 120, "Impact", 
        emptyList(), listOf(cardId1), listOf("tp_sql")
    )

    private fun createValidManifest(): PackageManifest {
        val baseManifest = PackageManifest(
            metadata = PackageMetadata(
                id = packageId,
                title = "SQL Course",
                description = "Learn SQL",
                version = PackageVersion(1, 0, 0),
                checksum = PackageChecksum("temp")
            ),
            topics = listOf(Topic("tp_sql", "SQL", "SQL")),
            cards = listOf(card1, card2)
        )
        val calculated = calculateChecksum(baseManifest)
        return baseManifest.copy(metadata = baseManifest.metadata.copy(checksum = calculated))
    }

    @Test
    fun testValidPackageImport() = runBlocking {
        val manifest = createValidManifest()
        val summary = workflow.execute(manifest)

        assertEquals("SUCCESS", summary.status)
        assertEquals(packageId, summary.packageId)
        assertEquals(2, summary.cardsImported)
        assertEquals(1, summary.topicsImported)

        // Verify state is saved
        assertNotNull(packageRepo.getPackage(packageId))
        assertNotNull(knowledgeRepo.getCard(cardId1))
        assertNotNull(knowledgeRepo.getCard(cardId2))
    }

    @Test
    fun testChecksumValidationFailure() = runBlocking {
        val manifest = createValidManifest()
        // Tamper with content without updating checksum
        val tamperedManifest = manifest.copy(topics = emptyList())

        try {
            workflow.execute(tamperedManifest)
            fail("Expected IllegalArgumentException due to invalid checksum")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Invalid checksum"))
        }
    }

    @Test
    fun testDuplicateIdValidation() = runBlocking {
        val baseManifest = PackageManifest(
            metadata = PackageMetadata(
                id = packageId,
                title = "SQL Course",
                description = "Learn SQL",
                version = PackageVersion(1, 0, 0),
                checksum = PackageChecksum("temp")
            ),
            topics = listOf(Topic("tp_sql", "SQL", "SQL"), Topic("tp_sql", "SQL2", "SQL")),
            cards = listOf(card1)
        )
        val calculated = calculateChecksum(baseManifest)
        val manifest = baseManifest.copy(metadata = baseManifest.metadata.copy(checksum = calculated))

        try {
            workflow.execute(manifest)
            fail("Expected IllegalArgumentException due to duplicate topic ID")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Duplicate Topic codes found"))
        }
    }

    @Test
    fun testMissingDependencyValidation() = runBlocking {
        // card2 requires card1 as prerequisite, but card1 is not in package or database
        val baseManifest = PackageManifest(
            metadata = PackageMetadata(
                id = packageId,
                title = "SQL Course",
                description = "Learn SQL",
                version = PackageVersion(1, 0, 0),
                checksum = PackageChecksum("temp")
            ),
            topics = listOf(Topic("tp_sql", "SQL", "SQL")),
            cards = listOf(card2)
        )
        val calculated = calculateChecksum(baseManifest)
        val manifest = baseManifest.copy(metadata = baseManifest.metadata.copy(checksum = calculated))

        try {
            workflow.execute(manifest)
            fail("Expected IllegalArgumentException due to missing prerequisite card")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Missing prerequisite dependency"))
        }
    }

    @Test
    fun testCircularDependencyValidation() = runBlocking {
        // card1 depends on card2, and card2 depends on card1
        val circularCard1 = card1.copy(prerequisiteCardIds = listOf(cardId2))
        val circularCard2 = card2.copy(prerequisiteCardIds = listOf(cardId1))

        val baseManifest = PackageManifest(
            metadata = PackageMetadata(
                id = packageId,
                title = "SQL Course",
                description = "Learn SQL",
                version = PackageVersion(1, 0, 0),
                checksum = PackageChecksum("temp")
            ),
            topics = listOf(Topic("tp_sql", "SQL", "SQL")),
            cards = listOf(circularCard1, circularCard2)
        )
        val calculated = calculateChecksum(baseManifest)
        val manifest = baseManifest.copy(metadata = baseManifest.metadata.copy(checksum = calculated))

        try {
            workflow.execute(manifest)
            fail("Expected IllegalArgumentException due to circular dependency")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Circular dependency detected"))
        }
    }

    @Test
    fun testVersionCompatibilityValidation() = runBlocking {
        // Save existing version to database
        val existingPkg = KnowledgePackage(
            id = packageId,
            title = "SQL Course",
            description = "Learn SQL",
            version = PackageVersion(2, 0, 0).toInt(),
            status = PackageStatus.PUBLISHED
        )
        packageRepo.pkg = existingPkg

        // Manifest tries to import older version (1.0.0)
        val manifest = createValidManifest() 

        try {
            workflow.execute(manifest)
            fail("Expected IllegalArgumentException due to downgrade attempt")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("older than existing version"))
        }
    }
}
