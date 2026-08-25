package com.learnwitme.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.learnwitme.core.domain.model.DifficultyLevel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var selectedDailyLimit by remember { mutableStateOf(30) }

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
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val currentState = state) {
                is OnboardingState.Idle -> {
                    Text(
                        text = "Welcome to LearnWitMe!",
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Kickstart your learning path today. Continue as guest to explore without signing in.",
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
                        text = "Learning Preferences",
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Set your daily target to personalize your learning pace.",
                        style = MaterialTheme.typography.bodyLarge,
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

                    Text("Daily Goal: $selectedDailyLimit minutes", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
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
                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            viewModel.savePreferences(
                                preferredTopics = listOf("tp_kotlin"),
                                dailyLimitMinutes = selectedDailyLimit,
                                targetLevel = DifficultyLevel.MID
                            )
                        },
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
                                    preferredTopics = listOf("tp_kotlin"),
                                    dailyLimitMinutes = selectedDailyLimit,
                                    targetLevel = DifficultyLevel.MID
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
