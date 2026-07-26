package com.learnwitme.core.designsystem.component

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.learnwitme.core.designsystem.foundation.LwmTheme

@Composable
fun LwmProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = modifier
            .fillMaxWidth()
            .height(LwmTheme.spacing.s),
        color = LwmTheme.colors.secondary,
        trackColor = LwmTheme.colors.surfaceVariant,
        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
    )
}

@Composable
fun LwmProgressRing(
    progress: Float? = null,
    modifier: Modifier = Modifier
) {
    if (progress != null) {
        CircularProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = modifier.defaultMinSize(
                minWidth = LwmTheme.dimensions.iconExtraLarge,
                minHeight = LwmTheme.dimensions.iconExtraLarge
            ),
            color = LwmTheme.colors.secondary,
            trackColor = LwmTheme.colors.surfaceVariant,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    } else {
        CircularProgressIndicator(
            modifier = modifier.defaultMinSize(
                minWidth = LwmTheme.dimensions.iconExtraLarge,
                minHeight = LwmTheme.dimensions.iconExtraLarge
            ),
            color = LwmTheme.colors.secondary,
            trackColor = LwmTheme.colors.surfaceVariant,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}
