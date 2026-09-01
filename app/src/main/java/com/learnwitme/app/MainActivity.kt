package com.learnwitme.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.unit.dp
import com.learnwitme.app.ui.startup.StartupState
import com.learnwitme.app.ui.startup.StartupViewModel
import com.learnwitme.core.config.ConfigManager
import com.learnwitme.core.navigation.*
import com.learnwitme.core.network.NetworkLogger
import com.learnwitme.core.storage.EncryptedDataStore
import com.learnwitme.core.theme.MetaTheme
import com.learnwitme.core.theme.ThemeManager
import com.learnwitme.developer.PerformanceMonitorOverlay
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import com.learnwitme.core.config.LocalizationManager

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var configManager: ConfigManager

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var navigationEngine: NavigationEngine

    @Inject
    lateinit var encryptedDataStore: EncryptedDataStore

    @Inject
    lateinit var networkLogger: NetworkLogger

    @Inject
    lateinit var localizationManager: LocalizationManager

    private val startupViewModel: StartupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MetaTheme(themeManager = themeManager) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val startupState by startupViewModel.state.collectAsState()
                    when (val state = startupState) {
                        is StartupState.Loading -> {
                            SplashScreen()
                        }
                        is StartupState.Error -> {
                            ErrorScreen(message = state.message, onRetry = startupViewModel::initialize)
                        }
                        is StartupState.NewGuestOnboarding,
                        is StartupState.PreferenceSetupRequired -> {
                            MainAppLayout(
                                configManager = configManager,
                                navigationEngine = navigationEngine,
                                encryptedDataStore = encryptedDataStore,
                                networkLogger = networkLogger,
                                localizationManager = localizationManager,
                                startDestination = OnboardingRoute
                            )
                        }
                        is StartupState.Ready -> {
                            MainAppLayout(
                                configManager = configManager,
                                navigationEngine = navigationEngine,
                                encryptedDataStore = encryptedDataStore,
                                networkLogger = networkLogger,
                                localizationManager = localizationManager,
                                startDestination = DailyFeedRoute
                            )
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("LearnWitMe", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text("Error Initializing App", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun MainAppLayout(
    configManager: ConfigManager,
    navigationEngine: NavigationEngine,
    encryptedDataStore: EncryptedDataStore,
    networkLogger: NetworkLogger,
    localizationManager: LocalizationManager,
    startDestination: Any
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentLang by localizationManager.currentLanguage.collectAsState()

    val menus = configManager.getActiveMenus().filter {
        it.route in listOf("home", "profile", "developer", "ai", "ocr")
    }
    val isPerfEnabled by encryptedDataStore.getStringFlow("dev_perf_overlay", "false")
        .collectAsState(initial = "false")

    Scaffold(
        bottomBar = {
            if (menus.isNotEmpty() && currentRoute in menus.map { getRouteClassName(it.route) }) {
                NavigationBar {
                    menus.forEach { menu ->
                        val selected = when (currentRoute) {
                            DailyFeedRoute::class.qualifiedName -> menu.route == "home"
                            ProfileRoute::class.qualifiedName -> menu.route == "profile"
                            else -> currentRoute == menu.route
                        }
                        val translatedTitle = localizationManager.getString("nav_" + menu.route, menu.title)
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                val dest = getRouteObjectForMenu(menu.route)
                                if (dest is String) {
                                    navController.navigate(dest) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                } else {
                                    navController.navigate(dest) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = getIconForName(menu.iconName),
                                    contentDescription = translatedTitle
                                )
                            },
                            label = { Text(translatedTitle) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
            ) {
                navigationEngine.setupNavigation(this, navController)
            }

            if (isPerfEnabled == "true") {
                PerformanceMonitorOverlay(
                    networkLogger = networkLogger,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 56.dp)
                )
            }
        }
    }
}

private fun getRouteClassName(routeStr: String): String? {
    return when (routeStr) {
        "home" -> DailyFeedRoute::class.qualifiedName
        "profile" -> ProfileRoute::class.qualifiedName
        else -> routeStr
    }
}

private fun getRouteObjectForMenu(routeStr: String): Any {
    return when (routeStr) {
        "home" -> DailyFeedRoute
        "profile" -> ProfileRoute
        else -> routeStr
    }
}

private fun getIconForName(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "home" -> Icons.Default.Home
        "setting", "settings" -> Icons.Default.Settings
        "profile", "person" -> Icons.Default.Person
        "history", "list" -> Icons.Default.List
        "premium", "star" -> Icons.Default.Star
        "developer", "build" -> Icons.Default.Build
        else -> Icons.Default.Home
    }
}
