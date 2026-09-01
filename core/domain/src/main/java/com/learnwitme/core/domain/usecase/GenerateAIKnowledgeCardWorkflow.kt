package com.learnwitme.core.domain.usecase

import com.learnwitme.core.domain.ai.AIProvider
import com.learnwitme.core.domain.ai.AIRequest
import com.learnwitme.core.domain.model.*
import com.learnwitme.core.domain.repository.KnowledgePackageRepository
import com.learnwitme.core.domain.repository.KnowledgeRepository
import com.learnwitme.core.domain.service.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GeneratedKnowledgeDto(
    val title: String = "",
    val summary: String = "",
    val whyItMatters: String = "",
    val realWorldExample: String = "",
    val commonMistakes: String = "",
    val whenNotToUse: String = "",
    val bestPractice: String = "",
    val careerImpact: String = "High",
    val estimatedReadingTimeSeconds: Int = 120,
    val references: List<String> = emptyList(),
    val officialDocumentationLink: String = ""
)

sealed interface AIKnowledgeGenerationResult {
    data class Success(val card: KnowledgeCard) : AIKnowledgeGenerationResult
    data class Failure(val reason: String) : AIKnowledgeGenerationResult
}

class GenerateAIKnowledgeCardWorkflow(
    private val aiProvider: AIProvider,
    private val knowledgeRepository: KnowledgeRepository,
    private val knowledgePackageRepository: KnowledgePackageRepository,
    private val clock: Clock
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun execute(
        packageId: KnowledgePackageId,
        topicCode: String,
        difficulty: DifficultyLevel = DifficultyLevel.BEGINNER,
        customPrompt: String? = null
    ): AIKnowledgeGenerationResult {
        val targetPackage = knowledgePackageRepository.getPackage(packageId)
            ?: return AIKnowledgeGenerationResult.Failure("Knowledge package ${packageId.value} not found")

        val prompt = customPrompt ?: "Generate a structured microlearning lesson about topic '$topicCode' for difficulty level '$difficulty'."
        val systemInstruction = """
            You are an educational AI assistant. Output ONLY valid JSON matching this schema:
            {
              "title": "Topic title",
              "summary": "Concise 1-2 sentence core concept",
              "whyItMatters": "Why this concept is critical in practice",
              "realWorldExample": "Concrete code/architecture scenario",
              "commonMistakes": "Top mistake developers make",
              "whenNotToUse": "When to avoid this approach",
              "bestPractice": "Golden rule/best practice",
              "careerImpact": "Impact on software engineering career",
              "estimatedReadingTimeSeconds": 120,
              "references": ["Ref 1"],
              "officialDocumentationLink": "https://..."
            }
        """.trimIndent()

        val aiResponse = try {
            aiProvider.generateLesson(
                AIRequest(
                    prompt = prompt,
                    systemInstruction = systemInstruction,
                    context = "Topic: $topicCode, Difficulty: $difficulty"
                )
            )
        } catch (e: Exception) {
            return AIKnowledgeGenerationResult.Failure("AI Provider failed: ${e.message}")
        }

        val dto = try {
            val rawContent = aiResponse.content.trim().removeSurrounding("```json", "```").trim()
            json.decodeFromString<GeneratedKnowledgeDto>(rawContent)
        } catch (e: Exception) {
            return AIKnowledgeGenerationResult.Failure("Failed to parse AI output into structured knowledge: ${e.message}")
        }

        // Quality & validation gate
        if (dto.title.isBlank() || dto.summary.isBlank() || dto.whyItMatters.isBlank()) {
            return AIKnowledgeGenerationResult.Failure("Validation failed: Required content fields cannot be blank")
        }

        // Duplicate detection: check if card with same title exists in package
        if (targetPackage.cards.any { it.title.equals(dto.title, ignoreCase = true) }) {
            return AIKnowledgeGenerationResult.Failure("Duplicate knowledge card title detected: ${dto.title}")
        }

        val generatedId = "c_ai_${topicCode}_${clock.currentTimeMillis()}"
        val validatedCard = KnowledgeCard(
            id = KnowledgeCardId(generatedId),
            packageId = packageId,
            title = dto.title.trim(),
            summary = dto.summary.trim(),
            whyItMatters = dto.whyItMatters.trim(),
            realWorldExample = dto.realWorldExample.trim(),
            commonMistakes = dto.commonMistakes.trim(),
            whenNotToUse = dto.whenNotToUse.trim(),
            bestPractice = dto.bestPractice.trim(),
            reference = dto.references.map { Reference(title = it) },
            officialDocumentationLink = dto.officialDocumentationLink.trim(),
            difficulty = difficulty,
            estimatedReadingTimeSeconds = dto.estimatedReadingTimeSeconds.coerceAtLeast(30),
            careerImpact = dto.careerImpact.trim(),
            tags = listOf(Tag("ai_generated"), Tag("model_${aiResponse.modelId.replace(" ", "_")}")),
            prerequisiteCardIds = emptyList(),
            relatedTopicCodes = listOf(topicCode)
        )

        // Save card and update package
        try {
            knowledgeRepository.saveCard(validatedCard)
            val updatedCards = targetPackage.cards + validatedCard
            knowledgePackageRepository.savePackage(targetPackage.copy(cards = updatedCards))
        } catch (e: Exception) {
            return AIKnowledgeGenerationResult.Failure("Failed to persist generated card: ${e.message}")
        }

        return AIKnowledgeGenerationResult.Success(validatedCard)
    }
}
