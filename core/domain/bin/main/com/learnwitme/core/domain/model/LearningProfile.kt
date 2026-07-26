package com.learnwitme.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LearningProfile(
    val learningGoal: String?, // Null for manual setup
    val careerPath: String,
    val currentLevel: DifficultyLevel,
    val targetLevel: DifficultyLevel,
    val detectedTechnologies: List<String>,
    val detectedInterests: List<String>,
    val recommendedTopics: List<String>,
    val recommendedRoadmap: String,
    val suggestedKnowledgePackages: List<String>,
    val estimatedLearningFocus: List<EstimatedFocus>
)

@Serializable
data class EstimatedFocus(
    val topic: String,
    val percentage: Float
)
