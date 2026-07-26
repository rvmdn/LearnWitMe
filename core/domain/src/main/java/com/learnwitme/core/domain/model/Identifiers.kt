package com.learnwitme.core.domain.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class UserId(val value: String) {
    init {
        require(value.isNotBlank()) { "UserId cannot be blank" }
    }
}

@Serializable
@JvmInline
value class KnowledgeCardId(val value: String) {
    init {
        require(value.isNotBlank()) { "KnowledgeCardId cannot be blank" }
    }
}

@Serializable
@JvmInline
value class LearningUnitId(val value: String) {
    init {
        require(value.isNotBlank()) { "LearningUnitId cannot be blank" }
    }
}

@Serializable
@JvmInline
value class QuizId(val value: String) {
    init {
        require(value.isNotBlank()) { "QuizId cannot be blank" }
    }
}

@Serializable
@JvmInline
value class QuizResultId(val value: String) {
    init {
        require(value.isNotBlank()) { "QuizResultId cannot be blank" }
    }
}

@Serializable
@JvmInline
value class LearningSessionId(val value: String) {
    init {
        require(value.isNotBlank()) { "LearningSessionId cannot be blank" }
    }
}

@Serializable
@JvmInline
value class LearningRoadmapId(val value: String) {
    init {
        require(value.isNotBlank()) { "LearningRoadmapId cannot be blank" }
    }
}

@Serializable
@JvmInline
value class LearningMemoryId(val value: String) {
    init {
        require(value.isNotBlank()) { "LearningMemoryId cannot be blank" }
    }
}

@Serializable
@JvmInline
value class KnowledgePackageId(val value: String) {
    init {
        require(value.isNotBlank()) { "KnowledgePackageId cannot be blank" }
    }
}

@Serializable
@JvmInline
value class QuizAttemptId(val value: String) {
    init {
        require(value.isNotBlank()) { "QuizAttemptId cannot be blank" }
    }
}

