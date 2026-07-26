package com.learnwitme.core.plugin

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PluginRegistry @Inject constructor(
    private val plugins: Set<@JvmSuppressWildcards MetaPlugin>
) {
    fun getPlugin(id: String): MetaPlugin? {
        return plugins.find { it.id.lowercase() == id.lowercase() }
    }

    fun getAllPlugins(): List<MetaPlugin> {
        return plugins.toList()
    }
}
