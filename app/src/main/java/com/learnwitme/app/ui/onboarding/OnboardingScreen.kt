package com.learnwitme.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.learnwitme.core.domain.model.DifficultyLevel

data class TopicOption(
    val code: String,
    val title: String,
    val description: String
)

val AVAILABLE_TOPICS = listOf(
    TopicOption("tp_kotlin", "Kotlin Fundamentals", "Core language syntax, OOP, & functional features"),
    TopicOption("tp_coroutine", "Coroutines & Flow", "Asynchronous programming & reactive state"),
    TopicOption("tp_compose", "Jetpack Compose", "Declarative modern Android UI development"),
    TopicOption("tp_architecture", "Clean Architecture", "Layering, SOLID, & domain modeling"),
    TopicOption("tp_storage", "Offline Storage", "Room DB, caching, & persistence"),
    TopicOption("tp_networking", "Networking & API", "HTTP clients, serialization, & sync")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var selectedTopics by remember { mutableStateOf(setOf("tp_kotlin")) }
    var selectedDailyLimit by remember { mutableStateOf(30) }
    var selectedTargetLevel by remember { mutableStateOf(DifficultyLevel.MID) }

    LaunchedEffect(state) {
        if (state is OnboardingState.Success) {
            onSuccess()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val currentState = state) {
                is OnboardingState.Idle -> {
                    Text(
                        text = "Welcome to LearnWitMe!",
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Kickstart your personalized learning path today. Continue as guest to explore without signing in.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { viewModel.continueAsGuest() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Continue as Guest")
                    }
                }

                is OnboardingState.PreferenceStep -> {
                    Text(
                        text = "Set Up Learning Path",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Choose the topics and pace that match your learning goals.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    if (currentState.error != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = currentState.error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 1. Topic Selection
                    Text(
                        text = "1. Select Topics of Interest",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AVAILABLE_TOPICS.forEach { topic ->
                            val isSelected = selectedTopics.contains(topic.code)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedTopics = if (isSelected) {
                                        if (selectedTopics.size > 1) selectedTopics - topic.code else selectedTopics
                                    } else {
                                        selectedTopics + topic.code
                                    }
                                },
                                label = { Text(topic.title) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 2. Daily Goal
                    Text(
                        text = "2. Daily Study Target: $selectedDailyLimit minutes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(15, 30, 45, 60).forEach { limit ->
                            FilterChip(
                                selected = selectedDailyLimit == limit,
                                onClick = { selectedDailyLimit = limit },
                                label = { Text("${limit}m") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 3. Target Level
                    Text(
                        text = "3. Target Difficulty Level",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            DifficultyLevel.BEGINNER to "Beginner",
                            DifficultyLevel.MID to "Intermediate",
                            DifficultyLevel.SENIOR to "Senior"
                        ).forEach { (level: DifficultyLevel, title: String) ->
                            FilterChip(
                                selected = selectedTargetLevel == level,
                                onClick = { selectedTargetLevel = level },
                                label = { Text(title) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            viewModel.savePreferences(
                                preferredTopics = selectedTopics.toList(),
                                dailyLimitMinutes = selectedDailyLimit,
                                targetLevel = selectedTargetLevel
                            )
                        },
                        enabled = selectedTopics.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save & Start Learning")
                    }
                }

                is OnboardingState.Loading -> {
                    CircularProgressIndicator()
                    if (!currentState.message.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = currentState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is OnboardingState.InitializingPersonalization -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Personalizing your daily learning plan...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is OnboardingState.Success -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Ready! Preparing your feed...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is OnboardingState.Error -> {
                    Text(
                        text = "Initialization Error",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentState.message,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (currentState.canRetryPreferences) {
                                viewModel.savePreferences(
                                    preferredTopics = selectedTopics.toList(),
                                    dailyLimitMinutes = selectedDailyLimit,
                                    targetLevel = selectedTargetLevel
                                )
                            } else {
                                viewModel.continueAsGuest()
                            }
                        }
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

