package com.learnwitme.core.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.learnwitme.core.ui.viewmodel.LearningUiState
import com.learnwitme.core.ui.viewmodel.LearningViewModel

@Composable
fun LearningScreenRoute(
    viewModel: LearningViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LearningScreen(
        uiState = uiState,
        modifier = modifier
    )
}

@Composable
fun LearningScreen(
    uiState: LearningUiState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is LearningUiState.Idle -> {
                Text(
                    text = "Select a card to begin learning session",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            is LearningUiState.Loading -> {
                CircularProgressIndicator()
            }
            is LearningUiState.ReviewSuccess -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Review Completed!",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Next review timestamp: ${uiState.event.nextReviewTimestamp}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            is LearningUiState.ActionSuccess -> {
                Text(
                    text = "Learning session updated successfully",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            is LearningUiState.Error -> {
                Text(
                    text = "Error: ${uiState.message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
