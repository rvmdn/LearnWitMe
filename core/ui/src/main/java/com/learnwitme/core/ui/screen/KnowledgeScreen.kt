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
import com.learnwitme.core.ui.viewmodel.KnowledgeUiState
import com.learnwitme.core.ui.viewmodel.KnowledgeViewModel

@Composable
fun KnowledgeScreenRoute(
    viewModel: KnowledgeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    KnowledgeScreen(
        uiState = uiState,
        modifier = modifier
    )
}

@Composable
fun KnowledgeScreen(
    uiState: KnowledgeUiState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is KnowledgeUiState.Idle -> {
                Text(
                    text = "Search knowledge cards or browse curriculum packages",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            is KnowledgeUiState.Loading -> {
                CircularProgressIndicator()
            }
            is KnowledgeUiState.CardSearchResults -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Found ${uiState.cards.size} matching cards",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            is KnowledgeUiState.CardDetailsLoaded -> {
                if (uiState.details != null) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = uiState.details.card.content.title,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.details.card.content.summary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Effective Difficulty: ${uiState.details.effectiveDifficulty}",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
            is KnowledgeUiState.PackageImported -> {
                Text(
                    text = "Package imported! ID: ${uiState.event.aggregateId}, Version: ${uiState.event.version}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            is KnowledgeUiState.PackagePublished -> {
                Text(
                    text = "Package published! Title: ${uiState.pkg.title}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            is KnowledgeUiState.Error -> {
                Text(
                    text = "Error: ${uiState.message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
