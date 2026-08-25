package com.learnwitme.core.domain.model.memory.personalization

import com.learnwitme.core.domain.model.FeedDecision
import com.learnwitme.core.domain.model.UserId

/**
 * Enumerates the high-level initialization states for a learner's daily learning session.
 */
enum class DailyInitializationState {
    NEW_GUEST_INITIALIZED,
    RETURNING_USER,
    NO_HISTORY_DEFAULT_ROADMAP,
    REVIEW_DUE_PRIORITIZED
}

/**
 * Responsibility: Typed result hierarchy representing the operational outcome of initializing
 * a personalized daily learning feed for a learner. Avoids uncontrolled exceptions for expected
 * incomplete or uninitialized states.
 */
sealed interface DailyLearningInitializationResult {

    /** Successful initialization containing a personalized LearningPersonalizationContext and FeedDecision. */
    data class Success(
        val userId: UserId,
        val context: LearningPersonalizationContext,
        val feedDecision: FeedDecision,
        val initializationState: DailyInitializationState
    ) : DailyLearningInitializationResult

    /** Typed state indicating that onboarding/personalization is incomplete. */
    data class IncompletePersonalization(
        val reason: PersonalizedContextResult.Incomplete
    ) : DailyLearningInitializationResult

    /** Valid empty/blocked state when no active knowledge package or roadmap content is available. */
    data class ContentUnavailable(
        val userId: UserId,
        val context: LearningPersonalizationContext,
        val reason: String = "No active knowledge package or roadmap content available"
    ) : DailyLearningInitializationResult
}
