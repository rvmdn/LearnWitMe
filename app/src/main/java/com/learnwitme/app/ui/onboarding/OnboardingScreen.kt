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

    if (state is OnboardingState.Success) {
        onSuccess()
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
            when (state) {
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
                }

                else -> {
                    if (state is OnboardingState.Error) {
                        Text(
                            text = (state as OnboardingState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.continueAsGuest() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}
