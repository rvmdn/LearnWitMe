package com.learnwitme.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.learnwitme.core.designsystem.foundation.LwmTheme

@Composable
fun LwmBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(LwmTheme.colors.secondaryContainer, LwmTheme.shapes.full)
            .padding(horizontal = LwmTheme.spacing.s, vertical = LwmTheme.spacing.xs)
    ) {
        Text(
            text = text,
            style = LwmTheme.typography.labelSmall,
            color = LwmTheme.colors.onSecondaryContainer
        )
    }
}

@Composable
fun LwmChip(
    label: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    FilterChip(
        selected = selected,
        onClick = { onSelectedChange(!selected) },
        label = { Text(text = label, style = LwmTheme.typography.labelMedium) },
        modifier = modifier,
        leadingIcon = if (icon != null) {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(end = LwmTheme.spacing.xs)
                )
            }
        } else null,
        shape = LwmTheme.shapes.medium,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = LwmTheme.colors.surfaceVariant,
            labelColor = LwmTheme.colors.onSurfaceVariant,
            selectedContainerColor = LwmTheme.colors.primary,
            selectedLabelColor = LwmTheme.colors.onPrimary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = LwmTheme.colors.outline,
            selectedBorderColor = LwmTheme.colors.primary
        )
    )
}

@Composable
fun LwmTag(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Box(
        modifier = modifier
            .background(LwmTheme.colors.primaryContainer, LwmTheme.shapes.small)
            .padding(horizontal = LwmTheme.spacing.s, vertical = LwmTheme.spacing.xs)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = LwmTheme.colors.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(LwmTheme.spacing.xs))
            }
            Text(
                text = text,
                style = LwmTheme.typography.labelSmall,
                color = LwmTheme.colors.onPrimaryContainer
            )
        }
    }
}
