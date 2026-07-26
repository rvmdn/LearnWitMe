package com.learnwitme.developer

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.learnwitme.core.config.ConfigManager
import com.learnwitme.core.storage.EncryptedDataStore
import com.learnwitme.core.storage.UserDao
import com.learnwitme.core.storage.UserEntity
import com.learnwitme.core.ui.components.DynamicCard
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperConsoleScreen(
    navController: NavHostController,
    configManager: ConfigManager,
    encryptedDataStore: EncryptedDataStore,
    userDao: UserDao
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var fakePremiumEnabled by remember { mutableStateOf(false) }

    var isMockEnabled by remember { mutableStateOf(false) }
    var isPerfEnabled by remember { mutableStateOf(false) }
    var dbUser by remember { mutableStateOf<UserEntity?>(null) }

    fun refreshDbUser() {
        coroutineScope.launch {
            dbUser = userDao.getUser()
        }
    }

    LaunchedEffect(Unit) {
        refreshDbUser()
    }

    LaunchedEffect(Unit) {
        encryptedDataStore.getStringFlow("dev_mock_api", "false").collect { value ->
            isMockEnabled = value == "true"
        }
    }

    LaunchedEffect(Unit) {
        encryptedDataStore.getStringFlow("dev_perf_overlay", "false").collect { value ->
            isPerfEnabled = value == "true"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer Console") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Build & Device Info", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DynamicCard(title = "Device Model", subtitle = "${Build.MANUFACTURER} ${Build.MODEL}", badgeText = "Device")
                    DynamicCard(title = "OS Version", subtitle = "Android SDK ${Build.VERSION.SDK_INT}", badgeText = "OS")
                    DynamicCard(title = "Build Mode", subtitle = "Debug: ${configManager.config.environment.debugEnabled}", badgeText = "Build")
                }
            }

            item {
                Text("Developer Toggles", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("API Mock Server Responses", style = MaterialTheme.typography.bodyLarge)
                            Text("Intercepts network requests and outputs dummy payloads", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = isMockEnabled,
                            onCheckedChange = { isChecked ->
                                coroutineScope.launch {
                                    encryptedDataStore.putString("dev_mock_api", isChecked.toString())
                                    Toast.makeText(context, "Mock API Responses: $isChecked", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Performance Monitor Overlay", style = MaterialTheme.typography.bodyLarge)
                            Text("Floating CPU, memory utilization, and network logs tracker", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = isPerfEnabled,
                            onCheckedChange = { isChecked ->
                                coroutineScope.launch {
                                    encryptedDataStore.putString("dev_perf_overlay", isChecked.toString())
                                    Toast.makeText(context, "Performance Overlay: $isChecked", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }

            item {
                Text("Room Database Inspector (SQLCipher)", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Table: users", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            dbUser?.let { user ->
                                Text("ID: ${user.id}", style = MaterialTheme.typography.bodyMedium)
                                Text("Name: ${user.name}", style = MaterialTheme.typography.bodyMedium)
                                Text("Email: ${user.email}", style = MaterialTheme.typography.bodyMedium)
                                Text("Premium Access: ${user.isPremium}", style = MaterialTheme.typography.bodyMedium)
                            } ?: Text(
                                "No user records found in encrypted database.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val testUser = UserEntity(
                                        id = "usr_test_1",
                                        name = "Irvan Ramadan Test",
                                        email = "test@learnwitme.com",
                                        isPremium = true
                                    )
                                    userDao.insertUser(testUser)
                                    refreshDbUser()
                                    Toast.makeText(context, "Inserted simulated user!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Insert User")
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    userDao.clearUser()
                                    refreshDbUser()
                                    Toast.makeText(context, "Wiped database records!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Wipe Table")
                        }
                    }
                }
            }

            item {
                Text("Developer Actions", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DynamicCard(
                        title = "Fake Premium Toggler",
                        subtitle = "Status: ${if (fakePremiumEnabled) "Premium Activated" else "Free Account"}",
                        badgeText = "ACTION",
                        onClick = {
                            fakePremiumEnabled = !fakePremiumEnabled
                            Toast.makeText(context, "Fake Premium Set to: $fakePremiumEnabled", Toast.LENGTH_SHORT).show()
                        }
                    )

                    DynamicCard(
                        title = "Clear Cache & Local Storage",
                        subtitle = "Wipe cache files and restart metadata loader",
                        badgeText = "ACTION",
                        onClick = {
                            configManager.loadConfig()
                            Toast.makeText(context, "Config Reloaded & Cache cleared!", Toast.LENGTH_SHORT).show()
                        }
                    )

                    DynamicCard(
                        title = "Trigger Simulated ANR / Crash",
                        subtitle = "Simulate a runtime crash to test error recovery",
                        badgeText = "CRASH",
                        onClick = {
                            throw RuntimeException("Simulated developer console crash test")
                        }
                    )
                }
            }

            item {
                Text("Remote Config / API Endpoints", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    configManager.config.apiEndpoints.forEach { (endpoint, url) ->
                        DynamicCard(title = endpoint, subtitle = url, badgeText = "API")
                    }
                }
            }
        }
    }
}
