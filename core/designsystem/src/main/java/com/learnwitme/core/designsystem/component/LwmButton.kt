package com.learnwitme.core.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.learnwitme.core.designsystem.foundation.LwmTheme

@Composable
fun LwmButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(LwmTheme.dimensions.buttonHeightLarge)
            .defaultMinSize(minWidth = LwmTheme.dimensions.minTouchTarget),
        enabled = enabled,
        shape = LwmTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = LwmTheme.colors.primary,
            contentColor = LwmTheme.colors.onPrimary,
            disabledContainerColor = LwmTheme.colors.surfaceVariant,
            disabledContentColor = LwmTheme.colors.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = LwmTheme.elevation.level0,
            pressedElevation = LwmTheme.elevation.level1
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.defaultMinSize(
                        minWidth = LwmTheme.dimensions.iconMedium,
                        minHeight = LwmTheme.dimensions.iconMedium
                    )
                )
                Spacer(modifier = Modifier.width(LwmTheme.spacing.s))
            }
            Text(
                text = text,
                style = LwmTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun LwmOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(LwmTheme.dimensions.buttonHeightLarge)
            .defaultMinSize(minWidth = LwmTheme.dimensions.minTouchTarget),
        enabled = enabled,
        shape = LwmTheme.shapes.medium,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = LwmTheme.colors.primary,
            disabledContentColor = LwmTheme.colors.onSurfaceVariant
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.defaultMinSize(
                        minWidth = LwmTheme.dimensions.iconMedium,
                        minHeight = LwmTheme.dimensions.iconMedium
                    )
                )
                Spacer(modifier = Modifier.width(LwmTheme.spacing.s))
            }
            Text(
                text = text,
                style = LwmTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun LwmIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(
            minWidth = LwmTheme.dimensions.minTouchTarget,
            minHeight = LwmTheme.dimensions.minTouchTarget
        ),
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = LwmTheme.colors.onSurface,
            disabledContentColor = LwmTheme.colors.onSurfaceVariant
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.defaultMinSize(
                minWidth = LwmTheme.dimensions.iconLarge,
                minHeight = LwmTheme.dimensions.iconLarge
            )
        )
    }
}
