package com.learnwitme.feature.knowledgecard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.learnwitme.core.designsystem.component.LwmButton
import com.learnwitme.core.designsystem.component.LwmCard
import com.learnwitme.core.designsystem.component.LwmCodeBlock
import com.learnwitme.core.designsystem.component.LwmErrorState
import com.learnwitme.core.designsystem.component.LwmLoadingState
import com.learnwitme.core.designsystem.component.LwmOutlinedButton
import com.learnwitme.core.designsystem.component.LwmProgressBar
import com.learnwitme.core.designsystem.component.LwmSectionHeader
import com.learnwitme.core.designsystem.component.LwmTag
import com.learnwitme.core.designsystem.component.LwmTopAppBar
import com.learnwitme.core.designsystem.foundation.LwmIcons
import com.learnwitme.core.designsystem.foundation.LwmTheme
import com.learnwitme.core.domain.model.DifficultyLevel
import com.learnwitme.core.domain.model.KnowledgeCard
import com.learnwitme.core.domain.model.KnowledgeCardId

@Composable
fun KnowledgeCardScreen(
    state: KnowledgeCardState,
    onEvent: (KnowledgeCardEvent) -> Unit,
    onBack: () -> Unit
) {
    LwmTheme {
        Scaffold(
            topBar = {
                Column {
                    LwmTopAppBar(
                        title = "Knowledge Unit",
                        navigationIcon = LwmIcons.ArrowBack,
                        onNavigationClick = onBack
                    )
                    LwmProgressBar(progress = 1.0f)
                }
            },
            containerColor = LwmTheme.colors.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (state) {
                    is KnowledgeCardState.Loading -> {
                        LwmLoadingState(message = "Loading Knowledge Unit...")
                    }
                    is KnowledgeCardState.Error -> {
                        LwmErrorState(
                            message = state.message,
                            onRetry = {
                                // Default retry action
                            }
                        )
                    }
                    is KnowledgeCardState.Success -> {
                        KnowledgeCardContent(
                            card = state.card,
                            aiState = state.aiState,
                            onEvent = onEvent
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KnowledgeCardContent(
    card: KnowledgeCard,
    aiState: AiAssistanceState,
    onEvent: (KnowledgeCardEvent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(LwmTheme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(LwmTheme.spacing.m)
    ) {
        // 1. Knowledge Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = card.title,
                    style = LwmTheme.typography.headlineMedium,
                    color = LwmTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(LwmTheme.spacing.s))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(LwmTheme.spacing.xs),
                    verticalArrangement = Arrangement.spacedBy(LwmTheme.spacing.xs)
                ) {
                    LwmTag(text = card.difficulty.name)
                    LwmTag(text = "${card.estimatedReadingTimeSeconds / 60} min read")
                    card.tags.forEach { tag ->
                        LwmTag(text = tag.name)
                    }
                }
            }
        }

        // 2. Summary
        item {
            Column {
                LwmSectionHeader(title = "Summary")
                LwmCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = card.summary,
                        style = LwmTheme.typography.bodyLarge,
                        color = LwmTheme.colors.onSurface
                    )
                }
            }
        }

        // 3. Why It Matters
        item {
            Column {
                LwmSectionHeader(title = "Why It Matters")
                LwmCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = card.whyItMatters,
                        style = LwmTheme.typography.bodyMedium,
                        color = LwmTheme.colors.onSurface
                    )
                }
            }
        }

        // 4. Code Block / Real World Example
        if (card.realWorldExample.isNotBlank()) {
            item {
                Column {
                    LwmSectionHeader(title = "Real World Example")
                    LwmCodeBlock(
                        code = card.realWorldExample,
                        language = "kotlin"
                    )
                }
            }
        }

        // 5. Best Practices & Common Mistakes
        if (card.bestPractice.isNotBlank()) {
            item {
                Column {
                    LwmSectionHeader(title = "Best Practice")
                    LwmCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = card.bestPractice,
                            style = LwmTheme.typography.bodyMedium,
                            color = LwmTheme.colors.onSurface
                        )
                    }
                }
            }
        }

        if (card.commonMistakes.isNotBlank()) {
            item {
                Column {
                    LwmSectionHeader(title = "Common Mistakes")
                    LwmCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = card.commonMistakes,
                            style = LwmTheme.typography.bodyMedium,
                            color = LwmTheme.colors.error
                        )
                    }
                }
            }
        }

        if (card.whenNotToUse.isNotBlank()) {
            item {
                Column {
                    LwmSectionHeader(title = "When Not To Use")
                    LwmCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = card.whenNotToUse,
                            style = LwmTheme.typography.bodyMedium,
                            color = LwmTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 6. Career Impact
        if (card.careerImpact.isNotBlank()) {
            item {
                Column {
                    LwmSectionHeader(title = "Career Impact")
                    LwmCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = card.careerImpact,
                            style = LwmTheme.typography.bodyMedium,
                            color = LwmTheme.colors.onSurface
                        )
                    }
                }
            }
        }

        // 7. Official References
        if (card.references.isNotEmpty() || card.officialDocumentationLink.isNotBlank()) {
            item {
                Column {
                    LwmSectionHeader(title = "Official References")
                    Column(verticalArrangement = Arrangement.spacedBy(LwmTheme.spacing.xs)) {
                        card.references.forEach { ref ->
                            LwmCard(modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    Text(
                                        text = ref.title,
                                        style = LwmTheme.typography.titleSmall,
                                        color = LwmTheme.colors.onSurface
                                    )
                                    ref.url?.let { url ->
                                        Text(
                                            text = url,
                                            style = LwmTheme.typography.bodySmall,
                                            color = LwmTheme.colors.secondary
                                        )
                                    }
                                }
                            }
                        }
                        if (card.officialDocumentationLink.isNotBlank()) {
                            LwmTag(text = "Doc: ${card.officialDocumentationLink}")
                        }
                    }
                }
            }
        }

        // 8. Related Knowledge Topics
        if (card.relatedTopicCodes.isNotEmpty()) {
            item {
                Column {
                    LwmSectionHeader(title = "Related Topics")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(LwmTheme.spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(LwmTheme.spacing.xs)
                    ) {
                        card.relatedTopicCodes.forEach { topicCode ->
                            LwmTag(text = topicCode)
                        }
                    }
                }
            }
        }

        // 9. Contextual AI Learning Assistance Section
        item {
            Column {
                LwmSectionHeader(title = "AI Learning Companion")
                when (aiState) {
                    is AiAssistanceState.Idle -> {
                        LwmCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(LwmTheme.spacing.s)
                            ) {
                                Text(
                                    text = "Need a deeper explanation or real-world clarification for this topic?",
                                    style = LwmTheme.typography.bodyMedium,
                                    color = LwmTheme.colors.onSurface
                                )
                                LwmOutlinedButton(
                                    text = "Ask AI for Explanation",
                                    onClick = { onEvent(KnowledgeCardEvent.OnRequestAiExplanation(card.id.value)) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    is AiAssistanceState.Loading -> {
                        LwmCard(modifier = Modifier.fillMaxWidth()) {
                            LwmLoadingState(message = "Generating personalized AI explanation...")
                        }
                    }
                    is AiAssistanceState.Success -> {
                        LwmCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(LwmTheme.spacing.s)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LwmTag(text = "AI Explanation")
                                    LwmOutlinedButton(
                                        text = "Dismiss",
                                        onClick = { onEvent(KnowledgeCardEvent.OnDismissAiExplanation) }
                                    )
                                }
                                Text(
                                    text = aiState.explanation,
                                    style = LwmTheme.typography.bodyMedium,
                                    color = LwmTheme.colors.onSurface
                                )
                                Spacer(modifier = Modifier.height(LwmTheme.spacing.xs))
                                if (!aiState.feedbackSubmitted) {
                                    Text(
                                        text = "Was this explanation helpful?",
                                        style = LwmTheme.typography.bodySmall,
                                        color = LwmTheme.colors.onSurfaceVariant
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(LwmTheme.spacing.s)
                                    ) {
                                        LwmOutlinedButton(
                                            text = "👍 Helpful",
                                            onClick = {
                                                onEvent(KnowledgeCardEvent.OnSubmitAiFeedback(aiState.interactionId, 1.0))
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                        LwmOutlinedButton(
                                            text = "👎 Needs Work",
                                            onClick = {
                                                onEvent(KnowledgeCardEvent.OnSubmitAiFeedback(aiState.interactionId, 0.2))
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                } else {
                                    LwmTag(text = "Feedback Recorded ✓")
                                }
                            }
                        }
                    }
                    is AiAssistanceState.Error -> {
                        LwmCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(LwmTheme.spacing.s)
                            ) {
                                Text(
                                    text = "AI Assistance Unavailable: ${aiState.message}",
                                    style = LwmTheme.typography.bodyMedium,
                                    color = LwmTheme.colors.error
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(LwmTheme.spacing.s)
                                ) {
                                    LwmButton(
                                        text = "Retry AI",
                                        onClick = { onEvent(KnowledgeCardEvent.OnRequestAiExplanation(card.id.value)) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    LwmOutlinedButton(
                                        text = "Dismiss",
                                        onClick = { onEvent(KnowledgeCardEvent.OnDismissAiExplanation) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 10. Primary CTA - Review Evaluation Bar
        item {
            LwmCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Rate Knowledge Retention",
                        style = LwmTheme.typography.titleMedium,
                        color = LwmTheme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(LwmTheme.spacing.s))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(LwmTheme.spacing.s)
                    ) {
                        LwmOutlinedButton(
                            text = "Forgot (0.2)",
                            onClick = { onEvent(KnowledgeCardEvent.OnSubmitReview(card.id.value, 0.2)) },
                            modifier = Modifier.weight(1f)
                        )
                        LwmOutlinedButton(
                            text = "Good (0.6)",
                            onClick = { onEvent(KnowledgeCardEvent.OnSubmitReview(card.id.value, 0.6)) },
                            modifier = Modifier.weight(1f)
                        )
                        LwmButton(
                            text = "Perfect (1.0)",
                            onClick = { onEvent(KnowledgeCardEvent.OnSubmitReview(card.id.value, 1.0)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Light Mode")
@Composable
private fun KnowledgeCardScreenPreviewLight() {
    val sampleCard = KnowledgeCard(
        id = KnowledgeCardId("c_kotlin_basics"),
        title = "Kotlin Basics & Syntax",
        summary = "Atomic summary of Kotlin core features and null safety.",
        whyItMatters = "Understanding null safety prevents NullPointerExceptions at runtime.",
        realWorldExample = "val name: String? = null\nprintln(name?.length ?: 0)",
        commonMistakes = "Overusing force non-null assertion (!!).",
        whenNotToUse = "Do not bypass compiler null checks without necessity.",
        bestPractice = "Prefer safe calls (?.) and Elvis operator (?:).",
        reference = emptyList(),
        officialDocumentationLink = "https://kotlinlang.org/docs",
        difficulty = DifficultyLevel.BEGINNER,
        estimatedReadingTimeSeconds = 300,
        careerImpact = "Essential foundation for modern Android development.",
        tags = emptyList(),
        prerequisiteCardIds = emptyList(),
        relatedTopicCodes = listOf("Kotlin", "Android")
    )
    LwmTheme(darkTheme = false) {
        Scaffold { padding ->
            Box(modifier = Modifier.padding(padding)) {
                KnowledgeCardContent(card = sampleCard, aiState = AiAssistanceState.Idle, onEvent = {})
            }
        }
    }
}

@Preview(name = "Dark Mode")
@Composable
private fun KnowledgeCardScreenPreviewDark() {
    val sampleCard = KnowledgeCard(
        id = KnowledgeCardId("c_kotlin_basics"),
        title = "Kotlin Basics & Syntax",
        summary = "Atomic summary of Kotlin core features and null safety.",
        whyItMatters = "Understanding null safety prevents NullPointerExceptions at runtime.",
        realWorldExample = "val name: String? = null\nprintln(name?.length ?: 0)",
        commonMistakes = "Overusing force non-null assertion (!!).",
        whenNotToUse = "Do not bypass compiler null checks without necessity.",
        bestPractice = "Prefer safe calls (?.) and Elvis operator (?:).",
        reference = emptyList(),
        officialDocumentationLink = "https://kotlinlang.org/docs",
        difficulty = DifficultyLevel.BEGINNER,
        estimatedReadingTimeSeconds = 300,
        careerImpact = "Essential foundation for modern Android development.",
        tags = emptyList(),
        prerequisiteCardIds = emptyList(),
        relatedTopicCodes = listOf("Kotlin", "Android")
    )
    LwmTheme(darkTheme = true) {
        Scaffold { padding ->
            Box(modifier = Modifier.padding(padding)) {
                KnowledgeCardContent(card = sampleCard, aiState = AiAssistanceState.Idle, onEvent = {})
            }
        }
    }
}
