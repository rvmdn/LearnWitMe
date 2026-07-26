package com.learnwitme.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class DifficultyLevel {
    BEGINNER, JUNIOR, MID, SENIOR, STAFF, PRINCIPAL
}
