package com.learnwitme.feature.dailyfeed

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.learnwitme.core.designsystem.component.LwmHeroCard
import com.learnwitme.core.designsystem.component.LwmLoadingState
import com.learnwitme.core.designsystem.component.LwmMetricCard
import com.learnwitme.core.designsystem.component.LwmOutlinedButton
import com.learnwitme.core.designsystem.component.LwmSectionHeader
import com.learnwitme.core.designsystem.component.LwmTopAppBar
import com.learnwitme.core.designsystem.foundation.LwmIcons
import com.learnwitme.core.designsystem.foundation.LwmTheme
import com.learnwitme.core.domain.model.FeedDecision
import com.learnwitme.core.domain.model.FeedSectionType

@Composable
fun DailyFeedScreen(
    state: DailyFeedState,
    onEvent: (DailyFeedEvent) -> Unit,
    onNavigateToQuiz: (String) -> Unit
) {
    LwmTheme {
        Scaffold(
            topBar = {
                LwmTopAppBar(
                    title = "LearnWitMe",
                    actionIcon = LwmIcons.Refresh,
                    onActionClick = { onEvent(DailyFeedEvent.OnRefresh) },
                    isOffline = true
                )
            },
            containerColor = LwmTheme.colors.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (state) {
                    is DailyFeedState.Loading -> {
                        LwmLoadingState(message = "Loading your personalized daily feed...")
                    }
                    is DailyFeedState.IncompletePersonalization -> {
                        LwmEmptyState(
                            title = "Preferences Required",
                            description = "Please complete your learning preferences to personalize your feed.",
                            actionText = "Set Preferences",
                            onActionClick = { onEvent(DailyFeedEvent.OnCompletePreferences) }
                        )
                    }
                    is DailyFeedState.ContentUnavailable -> {
                        LwmEmptyState(
                            title = "Content Unavailable",
                            description = state.reason.ifBlank { "No learning content is available for your current path." }
                        )
                    }
                    is DailyFeedState.Error -> {
                        LwmErrorState(
                            message = state.message,
                            onRetry = { onEvent(DailyFeedEvent.OnRefresh) }
                        )
                    }
                    is DailyFeedState.Success -> {
                        DailyFeedContent(
                            feed = state.feed,
                            onStartCard = { cardId -> onEvent(DailyFeedEvent.OnStartCard(cardId)) },
                            onNavigateToQuiz = onNavigateToQuiz
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun DailyFeedContent(
    feed: FeedDecision,
    onStartCard: (String) -> Unit,
    onNavigateToQuiz: (String) -> Unit
) {
    val sections = feed.feed.sections
    if (sections.isEmpty()) {
        LwmEmptyState(
            title = "No Feed Available",
            description = "Your feed is empty today! Check back later."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(LwmTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(LwmTheme.spacing.m)
        ) {
            items(sections) { section ->
                when (section.type) {
                    FeedSectionType.TODAYS_FOCUS -> {
                        val firstItem = section.items.firstOrNull()
                        val cardIdStr = firstItem?.cardId?.value ?: "c_kotlin_basics"
                        val formattedTitle = formatCardTitle(cardIdStr)
                        LwmHeroCard(
                            badgeText = "TODAY'S FOCUS",
                            title = formattedTitle,
                            subtitle = "Recommended 5-minute microlearning session",
                            ctaText = "Continue Learning",
                            onCtaClick = { onStartCard(cardIdStr) }
                        )
                    }
                    FeedSectionType.REVIEW_TODAY -> {
                        Column {
                            LwmSectionHeader(title = "Need Review")
                            LwmCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Spaced Repetition",
                                                style = LwmTheme.typography.titleMedium,
                                                color = LwmTheme.colors.onSurface
                                            )
                                            Text(
                                                text = "${section.items.size} item(s) due for review today",
                                                style = LwmTheme.typography.bodySmall,
                                                color = LwmTheme.colors.onSurfaceVariant
                                            )
                                        }
                                        if (section.items.isNotEmpty()) {
                                            LwmBadge(text = "${section.items.size} Due")
                                        }
                                    }
                                    if (section.items.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(LwmTheme.spacing.m))
                                        LwmOutlinedButton(
                                            text = "Start Reviewing",
                                            onClick = { onNavigateToQuiz(section.items.first().cardId.value) },
                                            modifier = Modifier.fillMaxWidth(),
                                            leadingIcon = LwmIcons.Review
                                        )
                                    }
                                }
                            }
                        }
                    }
                    FeedSectionType.PROGRESS -> {
                        val progress = section.summaryData
                        if (progress != null) {
                            Column {
                                LwmSectionHeader(title = "Learning Progress")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(LwmTheme.spacing.m)
                                ) {
                                    LwmMetricCard(
                                        title = "Streak",
                                        value = "${progress.currentStreak} Days",
                                        subtitle = "Consistency",
                                        icon = LwmIcons.Streak,
                                        modifier = Modifier.weight(1f)
                                    )
                                    LwmMetricCard(
                                        title = "Completion",
                                        value = "${(progress.completionPercentage * 100).toInt()}%",
                                        subtitle = "Mastery: ${(progress.masteryPercentage * 100).toInt()}%",
                                        icon = LwmIcons.Check,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                    FeedSectionType.CONTINUE_LEARNING -> {
                        Column {
                            LwmSectionHeader(title = "Continue Learning")
                            Column(verticalArrangement = Arrangement.spacedBy(LwmTheme.spacing.s)) {
                                section.items.take(3).forEach { item ->
                                    val title = formatCardTitle(item.cardId.value)
                                    LwmCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = { onStartCard(item.cardId.value) }
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = title,
                                                    style = LwmTheme.typography.titleMedium,
                                                    color = LwmTheme.colors.onSurface
                                                )
                                                Text(
                                                    text = "Recently Opened",
                                                    style = LwmTheme.typography.bodySmall,
                                                    color = LwmTheme.colors.onSurfaceVariant
                                                )
                                            }
                                            LwmButton(
                                                text = "Resume",
                                                onClick = { onStartCard(item.cardId.value) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        Column {
                            LwmSectionHeader(title = section.title)
                            section.items.forEach { item ->
                                val title = formatCardTitle(item.cardId.value)
                                LwmCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = LwmTheme.spacing.xs),
                                    onClick = { onStartCard(item.cardId.value) }
                                ) {
                                    Text(
                                        text = title,
                                        style = LwmTheme.typography.bodyLarge,
                                        color = LwmTheme.colors.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatCardTitle(rawId: String): String {
    return rawId
        .removePrefix("c_")
        .split("_")
        .joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
}

@Preview(name = "Light Mode")
@Composable
private fun DailyFeedScreenPreviewLight() {
    LwmTheme(darkTheme = false) {
        Scaffold { padding ->
            Box(modifier = Modifier.padding(padding)) {
                LwmHeroCard(
                    badgeText = "TODAY'S FOCUS",
                    title = "Kotlin Basics",
                    subtitle = "Recommended 5-minute microlearning session",
                    ctaText = "Continue Learning",
                    onCtaClick = {}
                )
            }
        }
    }
}

@Preview(name = "Dark Mode")
@Composable
private fun DailyFeedScreenPreviewDark() {
    LwmTheme(darkTheme = true) {
        Scaffold { padding ->
            Box(modifier = Modifier.padding(padding)) {
                LwmHeroCard(
                    badgeText = "TODAY'S FOCUS",
                    title = "Kotlin Basics",
                    subtitle = "Recommended 5-minute microlearning session",
                    ctaText = "Continue Learning",
                    onCtaClick = {}
                )
            }
        }
    }
}
