package com.learnwitme.core.domain.ai.prompt

import com.learnwitme.core.domain.ai.runtime.AIExecutionContext
import com.learnwitme.core.domain.ai.runtime.AIExecutionPolicy
import kotlinx.serialization.Serializable

/**
 * Immutable request container for building a complete prompt via [PromptPipeline].
 */
@Serializable
data class PromptBuildRequest(
    val template: PromptTemplate,
    val variables: PromptVariables = PromptVariables.EMPTY,
    val executionContext: AIExecutionContext = AIExecutionContext.DEFAULT,
    val executionPolicy: AIExecutionPolicy = AIExecutionPolicy.DEFAULT
)
