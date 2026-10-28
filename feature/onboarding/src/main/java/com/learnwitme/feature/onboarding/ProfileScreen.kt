package com.learnwitme.feature.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.learnwitme.core.designsystem.component.LwmCard
import com.learnwitme.core.designsystem.component.LwmErrorState
import com.learnwitme.core.designsystem.component.LwmLoadingState
import com.learnwitme.core.designsystem.component.LwmMetricCard
import com.learnwitme.core.designsystem.component.LwmProgressRing
import com.learnwitme.core.designsystem.component.LwmSectionHeader
import com.learnwitme.core.designsystem.component.LwmTag
import com.learnwitme.core.designsystem.component.LwmTopAppBar
import com.learnwitme.core.designsystem.foundation.LwmIcons
import com.learnwitme.core.designsystem.foundation.LwmTheme
import com.learnwitme.core.domain.model.CareerPath
import com.learnwitme.core.domain.model.DifficultyLevel
import com.learnwitme.core.domain.model.LearningPreference
import com.learnwitme.core.domain.model.LearningProgress
import com.learnwitme.core.domain.model.UserProfile
import com.learnwitme.core.domain.model.UserId

@Composable
fun ProfileScreen(
    state: ProfileState,
    onEvent: (ProfileEvent) -> Unit,
    onBack: () -> Unit
) {
    LwmTheme {
        Scaffold(
            topBar = {
                LwmTopAppBar(
                    title = "Profile & Progress",
                    navigationIcon = LwmIcons.ArrowBack,
                    onNavigationClick = onBack
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
                    is ProfileState.Loading -> {
                        LwmLoadingState(message = "Loading Profile...")
                    }
                    is ProfileState.Error -> {
                        LwmErrorState(
                            message = state.message,
                            onRetry = { onEvent(ProfileEvent.OnLoadProfile) }
                        )
                    }
                    is ProfileState.Success -> {
                        ProfileContent(profile = state.profile)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileContent(
    profile: UserProfile
) {
    val progress = profile.progress
    val totalUnits = progress.totalUnitsCompleted.coerceAtLeast(1)
    val masteryRatio = ((progress.totalCardsMastered.toFloat() / totalUnits.toFloat()) * 100).toInt()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(LwmTheme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(LwmTheme.spacing.m)
    ) {
        // 1. User Header Card
        item {
            LwmCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(LwmTheme.dimensions.iconExtraLarge * 1.5f)
                            .background(LwmTheme.colors.primaryContainer, LwmTheme.shapes.full),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LwmIcons.Profile,
                            contentDescription = "Profile Avatar",
                            tint = LwmTheme.colors.primary,
                            modifier = Modifier.size(LwmTheme.dimensions.iconExtraLarge)
                        )
                    }
                    Spacer(modifier = Modifier.width(LwmTheme.spacing.m))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.careerPath.name,
                            style = LwmTheme.typography.headlineSmall,
                            color = LwmTheme.colors.onSurface
                        )
                        Text(
                            text = profile.careerPath.description,
                            style = LwmTheme.typography.bodySmall,
                            color = LwmTheme.colors.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(LwmTheme.spacing.s))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(LwmTheme.spacing.xs),
                            verticalArrangement = Arrangement.spacedBy(LwmTheme.spacing.xs)
                        ) {
                            LwmTag(text = "Current: ${profile.currentLevel.name}")
                            LwmTag(text = "Target: ${profile.targetLevel.name}")
                        }
                    }
                }
            }
        }

        // 2. Learning Statistics Grid
        item {
            Column {
                LwmSectionHeader(title = "Learning Statistics")
                Column(verticalArrangement = Arrangement.spacedBy(LwmTheme.spacing.m)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(LwmTheme.spacing.m)
                    ) {
                        LwmMetricCard(
                            title = "Streak",
                            value = "${progress.activeStreakDays} Days",
                            subtitle = "Active Consistency",
                            icon = LwmIcons.Streak,
                            modifier = Modifier.weight(1f)
                        )
                        LwmMetricCard(
                            title = "Units",
                            value = "${progress.totalUnitsCompleted}",
                            subtitle = "Completed",
                            icon = LwmIcons.Check,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(LwmTheme.spacing.m)
                    ) {
                        LwmMetricCard(
                            title = "Mastered",
                            value = "${progress.totalCardsMastered}",
                            subtitle = "Cards Mastered",
                            icon = LwmIcons.Code,
                            modifier = Modifier.weight(1f)
                        )
                        LwmMetricCard(
                            title = "Target",
                            value = "${profile.preference.dailyLimitMinutes}m",
                            subtitle = "Daily Goal",
                            icon = LwmIcons.Review,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3. Mastery Overview Card
        item {
            Column {
                LwmSectionHeader(title = "Mastery Overview")
                LwmCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LwmProgressRing(
                            progress = (progress.totalCardsMastered.toFloat() / totalUnits.toFloat()).coerceIn(0f, 1f)
                        )
                        Spacer(modifier = Modifier.width(LwmTheme.spacing.m))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$masteryRatio% Mastered Ratio",
                                style = LwmTheme.typography.titleMedium,
                                color = LwmTheme.colors.onSurface
                            )
                            Spacer(modifier = Modifier.height(LwmTheme.spacing.xs))
                            Text(
                                text = "Completed ${progress.totalUnitsCompleted} units across your ${profile.careerPath.name} learning journey.",
                                style = LwmTheme.typography.bodySmall,
                                color = LwmTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 4. Preferred Topics
        if (profile.preference.preferredTopics.isNotEmpty()) {
            item {
                Column {
                    LwmSectionHeader(title = "Preferred Topics")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(LwmTheme.spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(LwmTheme.spacing.xs)
                    ) {
                        profile.preference.preferredTopics.forEach { topic ->
                            LwmTag(text = topic)
                        }
                    }
                }
            }
        }

        // 5. Cloud Sync Status
        item {
            Column {
                LwmSectionHeader(title = "Cloud Account & Synchronization")
                LwmCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Offline-First Active",
                                style = LwmTheme.typography.titleMedium,
                                color = LwmTheme.colors.onSurface
                            )
                            Spacer(modifier = Modifier.height(LwmTheme.spacing.xs))
                            Text(
                                text = "All learning records, memories, and progress are stored encrypted on this device.",
                                style = LwmTheme.typography.bodySmall,
                                color = LwmTheme.colors.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(LwmTheme.spacing.s))
                        LwmTag(text = "Cloud Sync: Coming Soon")
                    }
                }
            }
        }
    }
}

@Preview(name = "Profile Light Mode")
@Composable
private fun ProfileScreenPreviewLight() {
    val sampleProfile = UserProfile(
        id = UserId("u_1"),
        careerPath = CareerPath("cp_android", "Android Engineer", "Specialized in Kotlin & Jetpack Compose"),
        preference = LearningPreference(listOf("Kotlin", "Compose", "Architecture"), 15),
        currentLevel = DifficultyLevel.JUNIOR,
        targetLevel = DifficultyLevel.SENIOR,
        progress = LearningProgress(
            totalUnitsCompleted = 25,
            totalCardsMastered = 18,
            activeStreakDays = 7,
            lastActiveDate = 1000L
        )
    )

    LwmTheme(darkTheme = false) {
        Scaffold { padding ->
            Box(modifier = Modifier.padding(padding)) {
                ProfileContent(profile = sampleProfile)
            }
        }
    }
}

@Preview(name = "Profile Dark Mode")
@Composable
private fun ProfileScreenPreviewDark() {
    val sampleProfile = UserProfile(
        id = UserId("u_1"),
        careerPath = CareerPath("cp_android", "Android Engineer", "Specialized in Kotlin & Jetpack Compose"),
        preference = LearningPreference(listOf("Kotlin", "Compose", "Architecture"), 15),
        currentLevel = DifficultyLevel.JUNIOR,
        targetLevel = DifficultyLevel.SENIOR,
        progress = LearningProgress(
            totalUnitsCompleted = 25,
            totalCardsMastered = 18,
            activeStreakDays = 7,
            lastActiveDate = 1000L
        )
    )

    LwmTheme(darkTheme = true) {
        Scaffold { padding ->
            Box(modifier = Modifier.padding(padding)) {
                ProfileContent(profile = sampleProfile)
            }
        }
    }
}
