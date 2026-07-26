package com.learnwitme.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.learnwitme.core.config.ConfigManager
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class NavigationEngine @Inject constructor(
    private val configManager: ConfigManager,
    private val screenProviders: Set<@JvmSuppressWildcards ScreenProvider>
) {
    fun setupNavigation(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController
    ) {
        val configuredRoutes = configManager.getActiveScreens().map { it.route }.toSet()
        Timber.d("Configured screens from metadata: %s", configuredRoutes)
        
        screenProviders.forEach { provider ->
            if (configuredRoutes.contains(provider.route)) {
                Timber.d("Registering route dynamically: %s", provider.route)
                provider.register(navGraphBuilder, navController)
            } else {
                Timber.w("Route '%s' is omitted in metadata, skipping registration.", provider.route)
            }
        }
    }
}
