package com.learnwitme.core.storage.local.entity

import kotlinx.serialization.Serializable

@Serializable
data class PreferenceDto(
    val preferredTopics: List<String>,
    val dailyTimeCommitmentMinutes: Int
)

@Serializable
data class ProgressDto(
    val totalUnitsCompleted: Int,
    val totalCardsMastered: Int,
    val activeStreakDays: Int,
    val lastActiveDate: Long
)

@Serializable
data class LearningGoalDto(
    val title: String,
    val careerPathCode: String,
    val targetLevel: String
)

@Serializable
data class MemoryReviewDto(
    val timestamp: Long,
    val accuracy: Double,
    val hintUsed: Boolean,
    val confidenceRating: Double
)

@Serializable
data class TagDto(val name: String)

@Serializable
data class ReferenceDto(val title: String, val url: String?)

