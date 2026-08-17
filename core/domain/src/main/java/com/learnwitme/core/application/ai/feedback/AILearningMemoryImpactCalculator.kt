package com.learnwitme.core.application.ai.feedback

/**
 * Pure deterministic calculator that computes learning memory impact signals
 * from an AI learning interaction and learner feedback.
 */
class AILearningMemoryImpactCalculator {

    /**
     * Computes the deterministic learning-memory impact.
     * Guaranteed to be side-effect free and deterministic without external clocks or RNG.
     */
    fun calculateImpact(
        interaction: AILearningInteraction,
        feedback: AILearningFeedback,
        outcome: AILearningOutcome? = null
    ): AILearningMemoryImpact {
        val status = outcome?.status
        val rating = feedback.usefulnessRating
        val confidence = feedback.confidenceSignal

        val (classification, confidenceDelta) = when {
            // Outcome is explicitly skipped -> Neutral / No-Op
            status == AIOutcomeStatus.SKIPPED -> {
                AILearningImpactClassification.NEUTRAL_ENGAGEMENT to 0.0
            }

            // Outcome failed or rejected -> Remediation or Confidence reduction
            status == AIOutcomeStatus.FAILED || status == AIOutcomeStatus.REJECTED -> {
                if (rating <= 2) {
                    AILearningImpactClassification.REMEDIATION_SIGNAL to -0.05
                } else {
                    AILearningImpactClassification.CONFIDENCE_REDUCTION to -0.05
                }
            }

            // Completed / Partially Completed / Standard interaction outcome
            rating >= 4 -> {
                when {
                    confidence != null && confidence >= 0.70 -> {
                        AILearningImpactClassification.CONFIDENCE_BOOST to 0.10
                    }
                    confidence != null && confidence < 0.40 -> {
                        AILearningImpactClassification.CONFIDENCE_REDUCTION to -0.05
                    }
                    else -> {
                        AILearningImpactClassification.POSITIVE_REINFORCEMENT to 0.05
                    }
                }
            }

            rating <= 2 -> {
                AILearningImpactClassification.REMEDIATION_SIGNAL to -0.05
            }

            // Neutral rating (3)
            else -> {
                when {
                    confidence != null && confidence >= 0.80 -> {
                        AILearningImpactClassification.CONFIDENCE_BOOST to 0.05
                    }
                    confidence != null && confidence <= 0.30 -> {
                        AILearningImpactClassification.CONFIDENCE_REDUCTION to -0.05
                    }
                    else -> {
                        AILearningImpactClassification.NEUTRAL_ENGAGEMENT to 0.0
                    }
                }
            }
        }

        val calculationTimestamp = feedback.createdAt

        return AILearningMemoryImpact(
            interactionId = interaction.interactionId,
            cardId = interaction.cardId,
            topic = interaction.topic,
            action = interaction.action,
            outcomeStatus = status,
            usefulnessRating = rating,
            confidenceSignal = confidence,
            classification = classification,
            confidenceDelta = confidenceDelta,
            calculatedAt = calculationTimestamp
        )
    }
}
