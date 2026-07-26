package com.learnwitme.core.plugin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.learnwitme.core.config.ConfigManager
import com.learnwitme.core.ui.components.AdBanner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.learnwitme.core.config.LocalizationManager

class AiPlugin @Inject constructor(
    private val configManager: ConfigManager,
    private val localizationManager: LocalizationManager
) : MetaPlugin {
    override val id: String = "ai"
    override val name: String = "Generative AI Assistant"
    override val version: String = "2.1.0"

    private val messages = mutableStateListOf<Message>()

    data class Message(val sender: String, val text: String)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun PluginContent(
        modifier: Modifier,
        adsEnabled: Boolean,
        onPremiumUpgradeClick: () -> Unit
    ) {
        val coroutineScope = rememberCoroutineScope()
        var inputText by remember { mutableStateOf("") }
        var isTyping by remember { mutableStateOf(false) }
        val currentLang by localizationManager.currentLanguage.collectAsState()

        LaunchedEffect(currentLang) {
            if (messages.isEmpty()) {
                val initialGreeting = localizationManager.getString(
                    "ai_greeting",
                    "Hello! I am your AI Assistant powered by Gemini. How can I help you today?"
                )
                messages.add(Message("assistant", initialGreeting))
            }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = localizationManager.getString("ai_name", name),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            AdBanner(adsEnabled = adsEnabled, onRemoveAdsClick = onPremiumUpgradeClick)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    val isAssistant = msg.sender == "assistant"
                    val alignment = if (isAssistant) Alignment.Start else Alignment.End
                    val backgroundColor = if (isAssistant) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                    val contentColor = if (isAssistant) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = alignment
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = backgroundColor,
                                contentColor = contentColor
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isAssistant) {
                                localizationManager.getString("ai_name", "AI Assistant")
                            } else {
                                localizationManager.getString("ai_user", "You")
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }

                if (isTyping) {
                    item {
                        Text(
                            text = localizationManager.getString("ai_typing", "AI is typing..."),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text(localizationManager.getString("ai_placeholder", "Ask anything...")) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val userText = inputText
                            messages.add(Message("user", userText))
                            inputText = ""
                            isTyping = true
                            coroutineScope.launch {
                                delay(1000)
                                val response = simulateAiResponse(userText)
                                messages.add(Message("assistant", response))
                                isTyping = false
                            }
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = localizationManager.getString("ai_send", "Send message")
                    )
                }
            }
        }
    }

    private fun simulateAiResponse(prompt: String): String {
        val providers = configManager.config.aiProviders
        val activeProvider = providers.firstOrNull { it.enabled }?.name ?: "Local Engine"
        val lang = localizationManager.currentLanguage.value

        return if (lang == "id") {
            when {
                prompt.contains("halo", ignoreCase = true) || prompt.contains("hi", ignoreCase = true) || prompt.contains("hello", ignoreCase = true) ->
                    "Halo! Ada yang bisa saya bantu hari ini? Saya saat ini berjalan di atas $activeProvider."
                prompt.contains("tema", ignoreCase = true) || prompt.contains("theme", ignoreCase = true) ->
                    "Anda dapat mengubah tema sistem melalui halaman Pengaturan. Kami mendukung tema Hutan, Amoled, dan Glassmorphism!"
                prompt.contains("premium", ignoreCase = true) ->
                    "LearnWitMe Premium membuka perkakas khusus, menghapus iklan, dan mengaktifkan fitur OCR tambahan."
                else ->
                    "Saya menerima permintaan Anda: '$prompt'. Sebagai model AI yang berjalan di bawah konfigurasi $activeProvider, saya siap memproses permintaan Anda!"
            }
        } else {
            when {
                prompt.contains("hello", ignoreCase = true) || prompt.contains("hi", ignoreCase = true) ->
                    "Hello! How can I assist you today? I am currently running on $activeProvider."
                prompt.contains("theme", ignoreCase = true) ->
                    "You can change the system theme from the Settings page. We support Forest, Amoled, and Glassmorphism!"
                prompt.contains("premium", ignoreCase = true) ->
                    "LearnWitMe Premium unlocks special tools, removes advertisements, and enables additional OCR features."
                else ->
                    "I received your request: '$prompt'. As an AI model running under the $activeProvider configuration, I am ready to process your requests!"
            }
        }
    }
}
