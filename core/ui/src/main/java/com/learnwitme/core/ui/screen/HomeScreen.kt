package com.learnwitme.core.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.learnwitme.core.ui.viewmodel.HomeUiState
import com.learnwitme.core.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreenRoute(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        modifier = modifier
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is HomeUiState.Loading -> {
                CircularProgressIndicator()
            }
            is HomeUiState.Success -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Daily Learning Feed",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (uiState.feedResult != null) {
                        Text(
                            text = "Recommended Cards: ${uiState.feedResult.recommendations.size}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    if (uiState.topRecommendation != null) {
                        Text(
                            text = "Top Focus Card: ${uiState.topRecommendation.cardId.value}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            is HomeUiState.Error -> {
                Text(
                    text = "Error: ${uiState.message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
