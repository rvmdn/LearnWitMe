package com.learnwitme.core.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.learnwitme.core.designsystem.foundation.LwmTheme

@Composable
fun LwmSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = LwmTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = LwmTheme.typography.titleMedium,
            color = LwmTheme.colors.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    style = LwmTheme.typography.labelMedium,
                    color = LwmTheme.colors.secondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LwmTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null,
    isOffline: Boolean = false
) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = LwmTheme.typography.titleLarge,
                    color = LwmTheme.colors.onSurface
                )
                if (isOffline) {
                    LwmOfflineIndicator(modifier = Modifier.padding(start = LwmTheme.spacing.s))
                }
            }
        },
        modifier = modifier,
        navigationIcon = {
            if (navigationIcon != null && onNavigationClick != null) {
                LwmIconButton(
                    icon = navigationIcon,
                    contentDescription = "Back",
                    onClick = onNavigationClick
                )
            }
        },
        actions = {
            if (actionIcon != null && onActionClick != null) {
                LwmIconButton(
                    icon = actionIcon,
                    contentDescription = "Action",
                    onClick = onActionClick
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = LwmTheme.colors.surface,
            titleContentColor = LwmTheme.colors.onSurface
        )
    )
}
