package com.learnwitme.core.plugin

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.learnwitme.core.config.ConfigManager
import com.learnwitme.core.navigation.ScreenProvider
import javax.inject.Inject

class OcrScreenProvider @Inject constructor(
    private val configManager: ConfigManager,
    private val ocrPlugin: OcrPlugin
) : ScreenProvider {
    override val route: String = "ocr"

    override fun register(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {
        navGraphBuilder.composable(route) {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                ocrPlugin.PluginContent(
                    modifier = Modifier.padding(innerPadding),
                    adsEnabled = configManager.isFeatureEnabled("ads_enabled"),
                    onPremiumUpgradeClick = { navController.navigate("premium") }
                )
            }
        }
    }
}
