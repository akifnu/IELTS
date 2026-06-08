package com.flashcards.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flashcards.app.viewmodel.AccountViewModel
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(
    viewModel: AccountViewModel,
    snackbar: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var importText by remember { mutableStateOf("") }
    var showImportPaste by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText().orEmpty()
                viewModel.importBackup(json, onDone = { scope.launch { snackbar.showSnackbar("Backup restored") } }, onError = { scope.launch { snackbar.showSnackbar("Invalid backup") } })
            } catch (_: Exception) {
                snackbar.showSnackbar("Could not read file")
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val json = viewModel.exportBackup()
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                snackbar.showSnackbar("Backup saved")
            } catch (_: Exception) {
                snackbar.showSnackbar("Export failed")
            }
        }
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
    ) {
        item {
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("Your profile", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = state.settings.userName,
                        onValueChange = { viewModel.updateUserName(it) },
                        label = { Text("Display name") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        singleLine = true,
                    )
                    Text(
                        "${state.clusterCount} clusters · ${state.deckCount} decks on this device",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
        if (state.inbox.isNotEmpty()) {
            item { Text("Inbox", fontWeight = FontWeight.Bold) }
            items(state.inbox) { item ->
                Card {
                    Column(Modifier.padding(12.dp)) {
                        Text("Shared deck from ${item.fromName ?: item.fromEmail ?: "someone"}")
                        Row {
                            Button(onClick = {
                                viewModel.acceptInbox(item, null, onDone = { scope.launch { snackbar.showSnackbar("Deck added") } }, onError = { scope.launch { snackbar.showSnackbar("Import failed") } })
                            }) { Text("Accept") }
                            TextButton(onClick = { viewModel.dismissInbox(item.id) }) { Text("Dismiss") }
                        }
                    }
                }
            }
        }
        item {
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text("Backup", fontWeight = FontWeight.Bold)
                    Text("Export or import your full library as JSON", style = MaterialTheme.typography.bodySmall)
                    Button(
                        onClick = { exportLauncher.launch("shine-backup.json") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    ) { Text("Export backup") }
                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json", "text/*")) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    ) { Text("Import backup file") }
                    OutlinedButton(
                        onClick = { showImportPaste = !showImportPaste },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    ) { Text("Import shared deck JSON") }
                    if (showImportPaste) {
                        OutlinedTextField(
                            value = importText,
                            onValueChange = { importText = it },
                            label = { Text("Paste share JSON") },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            minLines = 3,
                        )
                        Button(
                            onClick = {
                                viewModel.importShare(importText, null,
                                    onDone = { importText = ""; showImportPaste = false; scope.launch { snackbar.showSnackbar("Deck imported") } },
                                    onError = { scope.launch { snackbar.showSnackbar("Invalid share data") } },
                                )
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        ) { Text("Import deck") }
                    }
                }
            }
        }
    }
}
