package com.learnwitme.core.model

import kotlinx.serialization.Serializable
import com.learnwitme.core.domain.model.DifficultyLevel

@Serializable
data class UserProfile(
    val careerPath: String,
    val currentLevel: DifficultyLevel,
    val targetLevel: DifficultyLevel,
    val preferredTopics: List<String>
)
