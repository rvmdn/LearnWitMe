package com.learnwitme.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.learnwitme.core.designsystem.foundation.LwmTheme

@Composable
fun LwmCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = LwmTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = LwmTheme.colors.surface,
                contentColor = LwmTheme.colors.onSurface
            ),
            border = BorderStroke(LwmTheme.dimensions.borderThin, LwmTheme.colors.outline),
            elevation = CardDefaults.cardElevation(defaultElevation = LwmTheme.elevation.level0),
            content = { Box(modifier = Modifier.padding(LwmTheme.spacing.m)) { content() } }
        )
    } else {
        Card(
            modifier = modifier,
            shape = LwmTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = LwmTheme.colors.surface,
                contentColor = LwmTheme.colors.onSurface
            ),
            border = BorderStroke(LwmTheme.dimensions.borderThin, LwmTheme.colors.outline),
            elevation = CardDefaults.cardElevation(defaultElevation = LwmTheme.elevation.level0),
            content = { Box(modifier = Modifier.padding(LwmTheme.spacing.m)) { content() } }
        )
    }
}

@Composable
fun LwmMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    LwmCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .background(LwmTheme.colors.primaryContainer, LwmTheme.shapes.medium)
                    .padding(LwmTheme.spacing.s),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = LwmTheme.colors.primary,
                    modifier = Modifier
                        .height(LwmTheme.dimensions.iconLarge)
                        .width(LwmTheme.dimensions.iconLarge)
                )
            }
            Spacer(modifier = Modifier.width(LwmTheme.spacing.m))
            Column {
                Text(
                    text = title.uppercase(),
                    style = LwmTheme.typography.labelSmall,
                    color = LwmTheme.colors.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = LwmTheme.typography.headlineMedium,
                    color = LwmTheme.colors.onSurface
                )
                Text(
                    text = subtitle,
                    style = LwmTheme.typography.bodySmall,
                    color = LwmTheme.colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LwmHeroCard(
    badgeText: String,
    title: String,
    subtitle: String,
    ctaText: String,
    onCtaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LwmCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            LwmBadge(text = badgeText)
            Spacer(modifier = Modifier.height(LwmTheme.spacing.s))
            Text(
                text = title,
                style = LwmTheme.typography.headlineSmall,
                color = LwmTheme.colors.onSurface
            )
            Spacer(modifier = Modifier.height(LwmTheme.spacing.xs))
            Text(
                text = subtitle,
                style = LwmTheme.typography.bodyMedium,
                color = LwmTheme.colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(LwmTheme.spacing.m))
            LwmButton(
                text = ctaText,
                onClick = onCtaClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
