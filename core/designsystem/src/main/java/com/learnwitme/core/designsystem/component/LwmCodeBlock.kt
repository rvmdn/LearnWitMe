package com.learnwitme.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.learnwitme.core.designsystem.foundation.LwmIcons
import com.learnwitme.core.designsystem.foundation.LwmTheme

@Composable
fun LwmCodeBlock(
    code: String,
    modifier: Modifier = Modifier,
    language: String? = null
) {
    val lines = code.trimEnd().lines()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LwmTheme.colors.surfaceVariant, LwmTheme.shapes.small)
            .padding(LwmTheme.spacing.m)
    ) {
        if (language != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = LwmTheme.spacing.s),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = LwmIcons.Code,
                    contentDescription = null,
                    tint = LwmTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(LwmTheme.spacing.xs))
                Text(
                    text = language.uppercase(),
                    style = LwmTheme.typography.codeSmall,
                    color = LwmTheme.colors.onSurfaceVariant
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            Column {
                lines.forEachIndexed { index, line ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${index + 1}".padStart(3),
                            style = LwmTheme.typography.codeSmall,
                            color = LwmTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(LwmTheme.spacing.m))
                        Text(
                            text = line,
                            style = LwmTheme.typography.codeMedium,
                            color = LwmTheme.colors.onSurface
                        )
                    }
                }
            }
        }
    }
}
