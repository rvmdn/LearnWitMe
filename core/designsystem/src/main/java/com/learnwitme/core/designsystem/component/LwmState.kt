package com.learnwitme.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.learnwitme.core.designsystem.foundation.LwmIcons
import com.learnwitme.core.designsystem.foundation.LwmTheme

@Composable
fun LwmEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = LwmIcons.Info,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(LwmTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LwmTheme.colors.onSurfaceVariant,
            modifier = Modifier.size(LwmTheme.dimensions.iconExtraLarge)
        )
        Spacer(modifier = Modifier.height(LwmTheme.spacing.m))
        Text(
            text = title,
            style = LwmTheme.typography.titleMedium,
            color = LwmTheme.colors.onSurface
        )
        Spacer(modifier = Modifier.height(LwmTheme.spacing.xs))
        Text(
            text = description,
            style = LwmTheme.typography.bodyMedium,
            color = LwmTheme.colors.onSurfaceVariant
        )
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(LwmTheme.spacing.l))
            LwmButton(text = actionText, onClick = onActionClick)
        }
    }
}

@Composable
fun LwmLoadingState(
    modifier: Modifier = Modifier,
    message: String = "Loading..."
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(LwmTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LwmProgressRing()
        Spacer(modifier = Modifier.height(LwmTheme.spacing.m))
        Text(
            text = message,
            style = LwmTheme.typography.bodyMedium,
            color = LwmTheme.colors.onSurfaceVariant
        )
    }
}

@Composable
fun LwmErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Something went wrong"
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(LwmTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = LwmIcons.Error,
            contentDescription = null,
            tint = LwmTheme.colors.error,
            modifier = Modifier.size(LwmTheme.dimensions.iconExtraLarge)
        )
        Spacer(modifier = Modifier.height(LwmTheme.spacing.m))
        Text(
            text = title,
            style = LwmTheme.typography.titleMedium,
            color = LwmTheme.colors.onSurface
        )
        Spacer(modifier = Modifier.height(LwmTheme.spacing.xs))
        Text(
            text = message,
            style = LwmTheme.typography.bodyMedium,
            color = LwmTheme.colors.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(LwmTheme.spacing.l))
        LwmOutlinedButton(
            text = "Retry",
            onClick = onRetry,
            leadingIcon = LwmIcons.Refresh
        )
    }
}

@Composable
fun LwmOfflineIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(LwmTheme.colors.surfaceVariant, LwmTheme.shapes.full)
            .padding(horizontal = LwmTheme.spacing.s, vertical = LwmTheme.spacing.xxs)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = LwmIcons.Offline,
                contentDescription = "Offline Mode",
                tint = LwmTheme.colors.secondary,
                modifier = Modifier.size(LwmTheme.dimensions.iconSmall)
            )
            Spacer(modifier = Modifier.width(LwmTheme.spacing.xs))
            Text(
                text = "Offline",
                style = LwmTheme.typography.labelSmall,
                color = LwmTheme.colors.onSurfaceVariant
            )
        }
    }
}
