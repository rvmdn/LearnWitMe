package com.learnwitme.core.application.ai.feedback

import com.learnwitme.core.application.ai.personalization.PersonalizedPromptPolicy
import com.learnwitme.core.application.ai.result.ExpandedKnowledgeCard
import com.learnwitme.core.application.ai.result.GeneratedExplanation
import com.learnwitme.core.application.ai.result.GeneratedLesson
import com.learnwitme.core.application.ai.result.GeneratedQuiz
import com.learnwitme.core.application.ai.result.SimplifiedContent
import com.learnwitme.core.application.usecase.ai.ExpandKnowledgeCardUseCase
import com.learnwitme.core.application.usecase.ai.ExplainAnswerUseCase
import com.learnwitme.core.application.usecase.ai.GenerateLessonUseCase
import com.learnwitme.core.application.usecase.ai.GenerateQuizUseCase
import com.learnwitme.core.application.usecase.ai.SimplifyTopicUseCase
import com.learnwitme.core.domain.ai.AIManager
import com.learnwitme.core.domain.ai.AIProviderType
import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.KnowledgeCardId
import com.learnwitme.core.domain.model.LearningMemory
import com.learnwitme.core.domain.model.Topic
import com.learnwitme.core.domain.model.UserId
import com.learnwitme.core.domain.model.memory.personalization.LearningPersonalizationContext

/**
 * Application-level coordinator / facade for AI learning interactions.
 *
 * Responsibilities:
 * - Executes AI learning use cases via composition
 * - Creates immutable AILearningInteraction and AILearningOutcome representations
 * - Preserves personalization and provider metadata
 * - Records optional feedback and projects Learning Memory impacts without persisting
 */
class AILearningInteractionService(
    private val generateLessonUseCase: GenerateLessonUseCase,
    private val generateQuizUseCase: GenerateQuizUseCase,
    private val explainAnswerUseCase: ExplainAnswerUseCase,
    private val expandKnowledgeCardUseCase: ExpandKnowledgeCardUseCase,
    private val simplifyTopicUseCase: SimplifyTopicUseCase,
    private val feedbackRecorder: AILearningFeedbackRecorder = AILearningFeedbackRecorder(),
    private val memoryIntegrator: AILearningMemoryFeedbackIntegrator = AILearningMemoryFeedbackIntegrator()
) {

    constructor(
        aiManager: AIManager,
        feedbackRecorder: AILearningFeedbackRecorder = AILearningFeedbackRecorder(),
        memoryIntegrator: AILearningMemoryFeedbackIntegrator = AILearningMemoryFeedbackIntegrator()
    ) : this(
        generateLessonUseCase = GenerateLessonUseCase(aiManager),
        generateQuizUseCase = GenerateQuizUseCase(aiManager),
        explainAnswerUseCase = ExplainAnswerUseCase(aiManager),
        expandKnowledgeCardUseCase = ExpandKnowledgeCardUseCase(aiManager),
        simplifyTopicUseCase = SimplifyTopicUseCase(aiManager),
        feedbackRecorder = feedbackRecorder,
        memoryIntegrator = memoryIntegrator
    )

    /**
     * Executes a Lesson Generation interaction.
     */
    suspend fun executeLesson(
        interactionId: String,
        userId: UserId,
        topic: String,
        startedAt: Long,
        completedAt: Long,
        cardId: KnowledgeCardId? = null,
        topicModel: Topic? = null,
        personalizationContext: LearningPersonalizationContext? = null,
        policy: PersonalizedPromptPolicy = PersonalizedPromptPolicy.LESSON_ONLY,
        feedback: AILearningFeedback? = null,
        currentMemory: LearningMemory? = null,
        provider: AIProviderType? = null,
        model: String? = null,
        promptTokens: Int = 0,
        completionTokens: Int = 0
    ): AILearningInteractionResult<GeneratedLesson> {
        val lesson = generateLessonUseCase(
            topic = topic,
            personalizationContext = personalizationContext,
            policy = policy
        )

        val interaction = AILearningInteraction(
            interactionId = interactionId,
            userId = userId,
            action = AIInteractionType.LESSON,
            cardId = cardId,
            topic = topicModel,
            startedAt = startedAt,
            completedAt = completedAt,
            provider = provider,
            model = model,
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            personalizationPreset = policy.presetName
        )

        val outcome = AILearningOutcome(
            interactionId = interactionId,
            status = AIOutcomeStatus.COMPLETED,
            summary = "Generated lesson for '$topic'",
            completedAt = completedAt
        )

        return processInteraction(lesson, interaction, outcome, feedback, currentMemory)
    }

    /**
     * Executes a Quiz Generation interaction for a specific KnowledgeCard.
     */
    suspend fun executeQuiz(
        interactionId: String,
        userId: UserId,
        card: KnowledgeCard,
        startedAt: Long,
        completedAt: Long,
        topicModel: Topic? = null,
        personalizationContext: LearningPersonalizationContext? = null,
        policy: PersonalizedPromptPolicy = PersonalizedPromptPolicy.QUIZ_ONLY,
        feedback: AILearningFeedback? = null,
        currentMemory: LearningMemory? = null,
        provider: AIProviderType? = null,
        model: String? = null,
        promptTokens: Int = 0,
        completionTokens: Int = 0
    ): AILearningInteractionResult<GeneratedQuiz> {
        val quiz = generateQuizUseCase(
            card = card,
            personalizationContext = personalizationContext,
            policy = policy
        )

        val interaction = AILearningInteraction(
            interactionId = interactionId,
            userId = userId,
            action = AIInteractionType.QUIZ,
            cardId = card.id,
            topic = topicModel,
            startedAt = startedAt,
            completedAt = completedAt,
            provider = provider,
            model = model,
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            personalizationPreset = policy.presetName
        )

        val outcome = AILearningOutcome(
            interactionId = interactionId,
            status = AIOutcomeStatus.COMPLETED,
            summary = "Generated quiz for card '${card.content.title}'",
            completedAt = completedAt
        )

        return processInteraction(quiz, interaction, outcome, feedback, currentMemory)
    }

    /**
     * Executes a Quiz Generation interaction for a general topic.
     */
    suspend fun executeQuiz(
        interactionId: String,
        userId: UserId,
        topic: String,
        startedAt: Long,
        completedAt: Long,
        cardId: KnowledgeCardId? = null,
        topicModel: Topic? = null,
        personalizationContext: LearningPersonalizationContext? = null,
        policy: PersonalizedPromptPolicy = PersonalizedPromptPolicy.QUIZ_ONLY,
        feedback: AILearningFeedback? = null,
        currentMemory: LearningMemory? = null,
        provider: AIProviderType? = null,
        model: String? = null,
        promptTokens: Int = 0,
        completionTokens: Int = 0
    ): AILearningInteractionResult<GeneratedQuiz> {
        val quiz = generateQuizUseCase(
            topic = topic,
            personalizationContext = personalizationContext,
            policy = policy
        )

        val interaction = AILearningInteraction(
            interactionId = interactionId,
            userId = userId,
            action = AIInteractionType.QUIZ,
            cardId = cardId,
            topic = topicModel,
            startedAt = startedAt,
            completedAt = completedAt,
            provider = provider,
            model = model,
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            personalizationPreset = policy.presetName
        )

        val outcome = AILearningOutcome(
            interactionId = interactionId,
            status = AIOutcomeStatus.COMPLETED,
            summary = "Generated quiz for topic '$topic'",
            completedAt = completedAt
        )

        return processInteraction(quiz, interaction, outcome, feedback, currentMemory)
    }

    /**
     * Executes an Answer Explanation interaction.
     */
    suspend fun executeExplanation(
        interactionId: String,
        userId: UserId,
        question: String,
        selectedAnswer: String,
        expectedAnswer: String,
        startedAt: Long,
        completedAt: Long,
        cardId: KnowledgeCardId? = null,
        topicModel: Topic? = null,
        personalizationContext: LearningPersonalizationContext? = null,
        policy: PersonalizedPromptPolicy = PersonalizedPromptPolicy.EXPLANATION_ONLY,
        feedback: AILearningFeedback? = null,
        currentMemory: LearningMemory? = null,
        provider: AIProviderType? = null,
        model: String? = null,
        promptTokens: Int = 0,
        completionTokens: Int = 0
    ): AILearningInteractionResult<GeneratedExplanation> {
        val explanation = explainAnswerUseCase(
            question = question,
            selectedAnswer = selectedAnswer,
            expectedAnswer = expectedAnswer,
            personalizationContext = personalizationContext,
            policy = policy
        )

        val interaction = AILearningInteraction(
            interactionId = interactionId,
            userId = userId,
            action = AIInteractionType.EXPLANATION,
            cardId = cardId,
            topic = topicModel,
            startedAt = startedAt,
            completedAt = completedAt,
            provider = provider,
            model = model,
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            personalizationPreset = policy.presetName
        )

        val outcome = AILearningOutcome(
            interactionId = interactionId,
            status = AIOutcomeStatus.COMPLETED,
            summary = "Generated explanation for question '$question'",
            completedAt = completedAt
        )

        return processInteraction(explanation, interaction, outcome, feedback, currentMemory)
    }

    /**
     * Executes a Knowledge Card Expansion interaction.
     */
    suspend fun executeKnowledgeExpansion(
        interactionId: String,
        userId: UserId,
        card: KnowledgeCard,
        startedAt: Long,
        completedAt: Long,
        topicModel: Topic? = null,
        personalizationContext: LearningPersonalizationContext? = null,
        policy: PersonalizedPromptPolicy = PersonalizedPromptPolicy.LESSON_ONLY,
        feedback: AILearningFeedback? = null,
        currentMemory: LearningMemory? = null,
        provider: AIProviderType? = null,
        model: String? = null,
        promptTokens: Int = 0,
        completionTokens: Int = 0
    ): AILearningInteractionResult<ExpandedKnowledgeCard> {
        val expandedCard = expandKnowledgeCardUseCase(
            card = card,
            personalizationContext = personalizationContext,
            policy = policy
        )

        val interaction = AILearningInteraction(
            interactionId = interactionId,
            userId = userId,
            action = AIInteractionType.KNOWLEDGE_EXPANSION,
            cardId = card.id,
            topic = topicModel,
            startedAt = startedAt,
            completedAt = completedAt,
            provider = provider,
            model = model,
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            personalizationPreset = policy.presetName
        )

        val outcome = AILearningOutcome(
            interactionId = interactionId,
            status = AIOutcomeStatus.COMPLETED,
            summary = "Expanded knowledge card '${card.content.title}'",
            completedAt = completedAt
        )

        return processInteraction(expandedCard, interaction, outcome, feedback, currentMemory)
    }

    /**
     * Executes a Technical Content Simplification interaction.
     */
    suspend fun executeSimplification(
        interactionId: String,
        userId: UserId,
        technicalContent: String,
        startedAt: Long,
        completedAt: Long,
        cardId: KnowledgeCardId? = null,
        topicModel: Topic? = null,
        personalizationContext: LearningPersonalizationContext? = null,
        policy: PersonalizedPromptPolicy = PersonalizedPromptPolicy.CONCISE,
        feedback: AILearningFeedback? = null,
        currentMemory: LearningMemory? = null,
        provider: AIProviderType? = null,
        model: String? = null,
        promptTokens: Int = 0,
        completionTokens: Int = 0
    ): AILearningInteractionResult<SimplifiedContent> {
        val simplified = simplifyTopicUseCase(
            technicalContent = technicalContent,
            personalizationContext = personalizationContext,
            policy = policy
        )

        val interaction = AILearningInteraction(
            interactionId = interactionId,
            userId = userId,
            action = AIInteractionType.SIMPLIFICATION,
            cardId = cardId,
            topic = topicModel,
            startedAt = startedAt,
            completedAt = completedAt,
            provider = provider,
            model = model,
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            personalizationPreset = policy.presetName
        )

        val outcome = AILearningOutcome(
            interactionId = interactionId,
            status = AIOutcomeStatus.COMPLETED,
            summary = "Simplified technical content",
            completedAt = completedAt
        )

        return processInteraction(simplified, interaction, outcome, feedback, currentMemory)
    }

    private fun <T> processInteraction(
        generatedResult: T,
        interaction: AILearningInteraction,
        outcome: AILearningOutcome?,
        feedback: AILearningFeedback?,
        currentMemory: LearningMemory?
    ): AILearningInteractionResult<T> {
        if (feedback == null) {
            return AILearningInteractionResult(
                generatedResult = generatedResult,
                interaction = interaction,
                outcome = outcome,
                feedback = null,
                feedbackResult = null,
                memoryIntegrationResult = null
            )
        }

        val recordResult = feedbackRecorder.recordFeedback(
            interaction = interaction,
            feedback = feedback,
            outcome = outcome
        )

        if (recordResult is AIInteractionFeedbackResult.Rejected) {
            return AILearningInteractionResult(
                generatedResult = generatedResult,
                interaction = interaction,
                outcome = outcome,
                feedback = feedback,
                feedbackResult = recordResult,
                memoryIntegrationResult = null
            )
        }

        val memoryResult = memoryIntegrator.integrateAccepted(
            acceptedResult = recordResult as AIInteractionFeedbackResult.Accepted,
            currentMemory = currentMemory
        )

        return AILearningInteractionResult(
            generatedResult = generatedResult,
            interaction = interaction,
            outcome = outcome,
            feedback = feedback,
            feedbackResult = recordResult,
            memoryIntegrationResult = memoryResult
        )
    }
}
