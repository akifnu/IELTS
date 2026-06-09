package com.flashcards.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flashcards.app.domain.ShineConstants
import com.flashcards.app.viewmodel.SettingsViewModel

private val ALGORITHM_INFO = mapOf(
    "ebbinghaus" to "Forgetting-curve spacing that adapts interval length from how well you recall cards.",
    "leitner" to "Cards move through numbered boxes — correct answers promote, misses send cards back.",
    "sm2" to "Classic SuperMemo-2 formula used by Anki; strong for long-term retention.",
)

private val PRESET_INFO = mapOf(
    "relaxed" to "Longer gaps between reviews. Good when you have time.",
    "normal" to "Balanced spacing for everyday study.",
    "intensive" to "Shorter intervals for exams or cramming periods.",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val settings by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                "Tune how Shine looks and how new decks schedule reviews.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        item {
            SettingsSection(title = "Appearance") {
                Text("Theme", fontWeight = FontWeight.SemiBold)
                Row(
                    Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ThemeChip(
                        selected = settings.themeMode == "system",
                        onClick = { viewModel.setThemeMode("system") },
                        icon = Icons.Default.PhoneAndroid,
                        label = "System",
                    )
                    ThemeChip(
                        selected = settings.themeMode == "light",
                        onClick = { viewModel.setThemeMode("light") },
                        icon = Icons.Default.LightMode,
                        label = "Light",
                    )
                    ThemeChip(
                        selected = settings.themeMode == "dark",
                        onClick = { viewModel.setThemeMode("dark") },
                        icon = Icons.Default.DarkMode,
                        label = "Dark",
                    )
                }
            }
        }

        item {
            SettingsSection(title = "Repetition algorithm") {
                Text(
                    "Defaults for new decks. You can still customize each deck individually.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Smart schedule", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Automatically space reviews",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = settings.defaultSmartSchedule,
                        onCheckedChange = viewModel::setDefaultSmartSchedule,
                    )
                }
                Text("Algorithm", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 20.dp))
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ShineConstants.ALGORITHMS.keys.forEach { key ->
                        FilterChip(
                            selected = settings.defaultAlgorithm == key,
                            onClick = { viewModel.setDefaultAlgorithm(key) },
                            label = { Text(ShineConstants.ALGORITHMS[key] ?: key) },
                        )
                    }
                }
                Text(
                    ALGORITHM_INFO[settings.defaultAlgorithm].orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text("Intensity preset", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 20.dp))
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("relaxed", "normal", "intensive").forEach { preset ->
                        FilterChip(
                            selected = settings.defaultPreset == preset,
                            onClick = { viewModel.setDefaultPreset(preset) },
                            label = { Text(preset.replaceFirstChar { it.uppercase() }) },
                        )
                    }
                }
                Text(
                    PRESET_INFO[settings.defaultPreset].orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        item {
            SettingsSection(title = "Calendar") {
                Text(
                    "Warn when more than this many deck sessions land on the same day.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Busy-day limit", fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.setMaxSessionsPerDay(settings.globalMaxSessionsPerDay - 1) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp)),
                        ) { Text("−", fontWeight = FontWeight.Bold) }
                        Text(
                            "${settings.globalMaxSessionsPerDay}",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                        IconButton(
                            onClick = { viewModel.setMaxSessionsPerDay(settings.globalMaxSessionsPerDay + 1) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp)),
                        ) { Text("+", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Column(Modifier.padding(top = 12.dp), content = content)
        }
    }
}

@Composable
private fun ThemeChip(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
    )
}
