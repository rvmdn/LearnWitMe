package com.learnwitme.core.plugin

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface MetaPlugin {
    val id: String
    val name: String
    val version: String
    
    @Composable
    fun PluginContent(
        modifier: Modifier,
        adsEnabled: Boolean,
        onPremiumUpgradeClick: () -> Unit
    )
}
