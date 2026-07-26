package com.learnwitme.core.plugin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.learnwitme.core.ui.components.AdBanner
import com.learnwitme.core.ui.components.DynamicImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class OcrPlugin @Inject constructor() : MetaPlugin {
    override val id: String = "ocr"
    override val name: String = "ML Kit OCR Scanner"
    override val version: String = "1.0.0"

    data class SampleImage(val url: String, val name: String, val text: String)

    private val samples = listOf(
        SampleImage(
            "https://picsum.photos/id/101/600/400",
            "Receipt Invoice",
            "INVOICE #94829\nDate: 2026-07-12\nTotal Amount: \$45.90\nThank you for shopping!"
        ),
        SampleImage(
            "https://picsum.photos/id/102/600/400",
            "Business Card",
            "Irvan Ramadan\nLead Android Architect\nEmail: irvan@learnwitme.com\nPhone: +62 812-3456-7890"
        ),
        SampleImage(
            "https://picsum.photos/id/103/600/400",
            "Signage Board",
            "CAUTION: ANTIGRAVITY ENGINE ACTIVE\nAuthorized personnel only. Safety goggles required."
        )
    )

    @Composable
    override fun PluginContent(
        modifier: Modifier,
        adsEnabled: Boolean,
        onPremiumUpgradeClick: () -> Unit
    ) {
        var selectedSample by remember { mutableStateOf<SampleImage?>(null) }
        var recognizedText by remember { mutableStateOf("") }
        var isProcessing by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            AdBanner(adsEnabled = adsEnabled, onRemoveAdsClick = onPremiumUpgradeClick)

            Text(
                text = "Select an Image to Scan:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(samples) { sample ->
                    val isSelected = selectedSample == sample
                    Card(
                        modifier = Modifier
                            .width(120.dp)
                            .clickable {
                                selectedSample = sample
                                recognizedText = ""
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            DynamicImage(
                                imageUrl = sample.url,
                                contentDescription = sample.name,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = sample.name,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            selectedSample?.let { sample ->
                DynamicImage(
                    imageUrl = sample.url,
                    contentDescription = sample.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isProcessing = true
                        coroutineScope.launch {
                            delay(1200)
                            recognizedText = sample.text
                            isProcessing = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProcessing
                ) {
                    Icon(
                        imageVector = Icons.Default.DocumentScanner,
                        contentDescription = "Recognize Text"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isProcessing) "Scanning Text..." else "Recognize Text")
                }
            } ?: Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No image selected",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isProcessing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (recognizedText.isNotEmpty()) {
                Text(
                    text = "Recognized Output:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = recognizedText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
