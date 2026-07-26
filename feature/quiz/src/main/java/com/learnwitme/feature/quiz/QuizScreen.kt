package com.learnwitme.feature.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.learnwitme.core.designsystem.component.LwmBadge
import com.learnwitme.core.designsystem.component.LwmButton
import com.learnwitme.core.designsystem.component.LwmCard
import com.learnwitme.core.designsystem.component.LwmEmptyState
import com.learnwitme.core.designsystem.component.LwmErrorState
import com.learnwitme.core.designsystem.component.LwmLoadingState
import com.learnwitme.core.designsystem.component.LwmMetricCard
import com.learnwitme.core.designsystem.component.LwmProgressBar
import com.learnwitme.core.designsystem.component.LwmSectionHeader
import com.learnwitme.core.designsystem.component.LwmTag
import com.learnwitme.core.designsystem.component.LwmTopAppBar
import com.learnwitme.core.designsystem.foundation.LwmIcons
import com.learnwitme.core.designsystem.foundation.LwmTheme
import com.learnwitme.core.domain.model.DifficultyLevel
import com.learnwitme.core.domain.model.ProgressSummary
import com.learnwitme.core.domain.model.Quiz
import com.learnwitme.core.domain.model.QuizAttempt
import com.learnwitme.core.domain.model.QuizAttemptId
import com.learnwitme.core.domain.model.QuizDecision
import com.learnwitme.core.domain.model.QuizId
import com.learnwitme.core.domain.model.QuizOption
import com.learnwitme.core.domain.model.QuizQuestion
import com.learnwitme.core.domain.model.QuizResult
import com.learnwitme.core.domain.model.QuizResultId
import com.learnwitme.core.domain.model.RecommendationDecision
import com.learnwitme.core.domain.model.UserId

@Composable
fun QuizScreen(
    state: QuizState,
    onEvent: (QuizEvent) -> Unit,
    onBack: () -> Unit
) {
    LwmTheme {
        val currentProgress = when (state) {
            is QuizState.Active -> {
                (state.currentQuestionIndex + 1).toFloat() / state.attempt.quiz.questions.size.toFloat()
            }
            is QuizState.Finished -> 1.0f
            else -> 0.0f
        }

        Scaffold(
            topBar = {
                Column {
                    LwmTopAppBar(
                        title = "Quiz Session",
                        navigationIcon = LwmIcons.ArrowBack,
                        onNavigationClick = onBack
                    )
                    LwmProgressBar(progress = currentProgress)
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
                    is QuizState.Idle -> {
                        LwmEmptyState(
                            title = "Quiz Ready",
                            description = "Tap start to begin your interactive quiz assessment."
                        )
                    }
                    is QuizState.Loading -> {
                        LwmLoadingState(message = "Preparing Quiz...")
                    }
                    is QuizState.Error -> {
                        LwmErrorState(
                            message = state.message,
                            onRetry = { /* Retry */ }
                        )
                    }
                    is QuizState.Active -> {
                        ActiveQuizContent(
                            state = state,
                            onEvent = onEvent
                        )
                    }
                    is QuizState.Finished -> {
                        FinishedQuizContent(
                            decision = state.decision,
                            onBack = onBack
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveQuizContent(
    state: QuizState.Active,
    onEvent: (QuizEvent) -> Unit
) {
    val attempt = state.attempt
    val index = state.currentQuestionIndex
    val totalQuestions = attempt.quiz.questions.size
    val question = attempt.quiz.questions[index]
    val answer = attempt.answers.find { it.questionId == question.id }
    val isSubmitted = answer != null

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(LwmTheme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(LwmTheme.spacing.m)
    ) {
        // 1. Question Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Question ${index + 1} of $totalQuestions",
                        style = LwmTheme.typography.titleMedium,
                        color = LwmTheme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(LwmTheme.spacing.xs))
                    LwmTag(text = attempt.quiz.targetDifficulty.name)
                }
                LwmBadge(text = "${((index + 1) * 100) / totalQuestions}% Completed")
            }
        }

        // 2. Question Card
        item {
            LwmCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = question.questionText,
                    style = LwmTheme.typography.headlineSmall,
                    color = LwmTheme.colors.onSurface
                )
            }
        }

        // 3. Answer Options
        item {
            LwmSectionHeader(title = "Options")
        }

        items(question.options.size) { optIndex ->
            val option = question.options[optIndex]
            val isSelected = answer?.selectedOptionIndex == optIndex
            val isCorrectOption = question.correctOptionIndex == optIndex

            val containerColor = when {
                isSubmitted && isSelected && isCorrectOption -> LwmTheme.colors.secondaryContainer
                isSubmitted && isSelected && !isCorrectOption -> LwmTheme.colors.errorContainer
                isSubmitted && !isSelected && isCorrectOption -> LwmTheme.colors.secondaryContainer.copy(alpha = 0.4f)
                !isSubmitted && isSelected -> LwmTheme.colors.primaryContainer
                else -> LwmTheme.colors.surface
            }

            val borderColor = when {
                isSubmitted && isCorrectOption -> LwmTheme.colors.secondary
                isSubmitted && isSelected && !isCorrectOption -> LwmTheme.colors.error
                !isSubmitted && isSelected -> LwmTheme.colors.primary
                else -> LwmTheme.colors.outline
            }

            val optionPrefix = ('A' + optIndex).toString()

            Card(
                onClick = {
                    if (!isSubmitted) {
                        onEvent(QuizEvent.OnSubmitAnswer(question.id, optIndex))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = LwmTheme.spacing.xs),
                shape = LwmTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = containerColor,
                    contentColor = LwmTheme.colors.onSurface
                ),
                border = BorderStroke(LwmTheme.dimensions.borderThin, borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(LwmTheme.spacing.m),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$optionPrefix.",
                        style = LwmTheme.typography.titleMedium,
                        color = LwmTheme.colors.primary,
                        modifier = Modifier.padding(end = LwmTheme.spacing.s)
                    )
                    Text(
                        text = option.text,
                        style = LwmTheme.typography.bodyLarge,
                        color = LwmTheme.colors.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSubmitted) {
                        if (isCorrectOption) {
                            Icon(
                                imageVector = LwmIcons.Check,
                                contentDescription = "Correct",
                                tint = LwmTheme.colors.secondary,
                                modifier = Modifier.size(LwmTheme.dimensions.iconMedium)
                            )
                        } else if (isSelected) {
                            Icon(
                                imageVector = LwmIcons.Error,
                                contentDescription = "Incorrect",
                                tint = LwmTheme.colors.error,
                                modifier = Modifier.size(LwmTheme.dimensions.iconMedium)
                            )
                        }
                    }
                }
            }
        }

        // 4. Feedback & Explanation (if submitted)
        if (isSubmitted) {
            val selectedIndex = answer.selectedOptionIndex
            val isCorrect = selectedIndex == question.correctOptionIndex
            val selectedOption = question.options.getOrNull(selectedIndex)

            item {
                LwmCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCorrect) LwmIcons.Check else LwmIcons.Error,
                                contentDescription = null,
                                tint = if (isCorrect) LwmTheme.colors.secondary else LwmTheme.colors.error
                            )
                            Spacer(modifier = Modifier.padding(start = LwmTheme.spacing.xs))
                            Text(
                                text = if (isCorrect) "✔ Correct Answer!" else "✘ Incorrect Answer",
                                style = LwmTheme.typography.titleMedium,
                                color = if (isCorrect) LwmTheme.colors.secondary else LwmTheme.colors.error
                            )
                        }
                        if (selectedOption?.explanation?.isNotBlank() == true) {
                            Spacer(modifier = Modifier.height(LwmTheme.spacing.s))
                            Text(
                                text = selectedOption.explanation,
                                style = LwmTheme.typography.bodyMedium,
                                color = LwmTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 5. Navigation Action Button
        item {
            Spacer(modifier = Modifier.height(LwmTheme.spacing.m))
            if (index < totalQuestions - 1) {
                LwmButton(
                    text = "Next Question",
                    onClick = { onEvent(QuizEvent.OnNextQuestion) },
                    enabled = isSubmitted,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                LwmButton(
                    text = "Finish Quiz",
                    onClick = { onEvent(QuizEvent.OnFinishQuiz) },
                    enabled = isSubmitted,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FinishedQuizContent(
    decision: QuizDecision,
    onBack: () -> Unit
) {
    val result = decision.result
    val progress = decision.updatedProgress
    val scorePercentage = (result.score * 100).toInt()
    val correctCount = result.answers.count { it.isCorrect }
    val totalCount = result.answers.size
    val recommendationReason = decision.updatedRecommendation.topRecommendation?.reasons?.firstOrNull()
        ?: "Keep practicing to solidify learning retention."

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(LwmTheme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(LwmTheme.spacing.m),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            LwmCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Quiz Finished!",
                        style = LwmTheme.typography.headlineMedium,
                        color = LwmTheme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(LwmTheme.spacing.s))
                    Text(
                        text = "$scorePercentage%",
                        style = LwmTheme.typography.displayMedium,
                        color = LwmTheme.colors.secondary
                    )
                    Text(
                        text = "$correctCount of $totalCount Correct",
                        style = LwmTheme.typography.titleMedium,
                        color = LwmTheme.colors.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LwmTheme.spacing.m)
            ) {
                LwmMetricCard(
                    title = "Correct",
                    value = "$correctCount / $totalCount",
                    subtitle = "Answers",
                    icon = LwmIcons.Check,
                    modifier = Modifier.weight(1f)
                )
                LwmMetricCard(
                    title = "Streak",
                    value = "${progress.currentStreak} Days",
                    subtitle = "Retention",
                    icon = LwmIcons.Streak,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            LwmCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Next Recommendation",
                        style = LwmTheme.typography.titleMedium,
                        color = LwmTheme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(LwmTheme.spacing.xs))
                    Text(
                        text = recommendationReason,
                        style = LwmTheme.typography.bodyMedium,
                        color = LwmTheme.colors.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(LwmTheme.spacing.l))
            LwmButton(
                text = "Back to Home",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(name = "Active Quiz Light Mode")
@Composable
private fun QuizScreenActivePreviewLight() {
    val sampleQuiz = Quiz(
        id = QuizId("q_kotlin_basics"),
        title = "Kotlin Basics Quiz",
        targetDifficulty = DifficultyLevel.BEGINNER,
        questions = listOf(
            QuizQuestion(
                id = "q1",
                questionText = "Which keyword is used for read-only variables in Kotlin?",
                options = listOf(
                    QuizOption("var", "var is for mutable variables"),
                    QuizOption("val", "val is for read-only (immutable) variables"),
                    QuizOption("const", "const is for compile-time constants"),
                    QuizOption("let", "let is a scope function")
                ),
                correctOptionIndex = 1
            )
        )
    )
    val attempt = QuizAttempt(
        id = QuizAttemptId("qa_1"),
        userId = UserId("u_1"),
        quiz = sampleQuiz
    )

    LwmTheme(darkTheme = false) {
        Scaffold { padding ->
            Box(modifier = Modifier.padding(padding)) {
                ActiveQuizContent(
                    state = QuizState.Active(attempt = attempt, currentQuestionIndex = 0),
                    onEvent = {}
                )
            }
        }
    }
}

@Preview(name = "Finished Quiz Dark Mode")
@Composable
private fun QuizScreenFinishedPreviewDark() {
    val result = QuizResult(
        id = QuizResultId("qres_1"),
        quizId = QuizId("q_1"),
        score = 0.8,
        answers = emptyList(),
        completedTimestamp = 1000L
    )
    val progress = ProgressSummary(
        completionPercentage = 0.8,
        masteryPercentage = 0.75,
        currentStreak = 5,
        dailyProgressUnits = 2,
        weeklyProgressUnits = 10,
        learningVelocityUnitsPerMinute = 0.5
    )
    val decision = QuizDecision(
        result = result,
        updatedProgress = progress,
        updatedRecommendation = RecommendationDecision(
            topRecommendation = null,
            alternatives = emptyList()
        )
    )

    LwmTheme(darkTheme = true) {
        Scaffold { padding ->
            Box(modifier = Modifier.padding(padding)) {
                FinishedQuizContent(
                    decision = decision,
                    onBack = {}
                )
            }
        }
    }
}
