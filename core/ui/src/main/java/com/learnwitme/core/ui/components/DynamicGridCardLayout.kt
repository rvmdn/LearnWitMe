package com.learnwitme.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun <T> DynamicGridCardLayout(
    items: List<T>,
    columnsCount: Int,
    modifier: Modifier = Modifier,
    spacing: Dp = 16.dp,
    itemContent: @Composable (T) -> Unit
) {
    if (columnsCount <= 1) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            items.forEach { item ->
                itemContent(item)
            }
        }
    } else {
        val rows = items.chunked(columnsCount)
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    rowItems.forEach { item ->
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            itemContent(item)
                        }
                    }
                    val emptyBoxesCount = columnsCount - rowItems.size
                    if (emptyBoxesCount > 0) {
                        repeat(emptyBoxesCount) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
