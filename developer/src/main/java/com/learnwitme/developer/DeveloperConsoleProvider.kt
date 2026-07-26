package com.learnwitme.developer

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.learnwitme.core.config.ConfigManager
import com.learnwitme.core.navigation.ScreenProvider
import javax.inject.Inject

class DeveloperConsoleProvider @Inject constructor(
    private val configManager: ConfigManager,
    private val encryptedDataStore: com.learnwitme.core.storage.EncryptedDataStore,
    private val userDao: com.learnwitme.core.storage.UserDao
) : ScreenProvider {
    override val route: String = "developer"

    override fun register(navGraphBuilder: NavGraphBuilder, navController: NavHostController) {
        navGraphBuilder.composable(route) {
            DeveloperConsoleScreen(
                navController = navController,
                configManager = configManager,
                encryptedDataStore = encryptedDataStore,
                userDao = userDao
            )
        }
    }
}
