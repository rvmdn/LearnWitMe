package com.learnwitme.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.learnwitme.core.designsystem.foundation.LwmTheme

data class LwmNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

@Composable
fun LwmBottomNavigation(
    items: List<LwmNavItem>,
    currentRoute: String,
    onNavItemClick: (LwmNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .height(LwmTheme.dimensions.bottomNavHeight),
        containerColor = LwmTheme.colors.surface,
        contentColor = LwmTheme.colors.onSurface,
        tonalElevation = LwmTheme.elevation.level0
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavItemClick(item) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = LwmTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = LwmTheme.colors.onPrimary,
                    selectedTextColor = LwmTheme.colors.primary,
                    indicatorColor = LwmTheme.colors.primary,
                    unselectedIconColor = LwmTheme.colors.onSurfaceVariant,
                    unselectedTextColor = LwmTheme.colors.onSurfaceVariant
                )
            )
        }
    }
}
