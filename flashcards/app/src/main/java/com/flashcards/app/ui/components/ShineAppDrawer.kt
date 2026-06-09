package com.flashcards.app.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flashcards.app.domain.Cluster
import com.flashcards.app.viewmodel.AccountUiState
import com.flashcards.app.viewmodel.AccountViewModel
import com.flashcards.app.viewmodel.HomeUiState
import kotlinx.coroutines.launch

enum class DrawerAction {
    Account,
    Inbox,
}

@Composable
fun ShineAppDrawer(
    state: AccountUiState,
    homeState: HomeUiState,
    selectedAction: DrawerAction?,
    onAction: (DrawerAction) -> Unit,
    onDismiss: () -> Unit,
    onOpenDeck: (Long) -> Unit,
    onStudyDeck: (Long) -> Unit,
    onAddDeckToCluster: (Long) -> Unit,
    onNewCluster: () -> Unit,
    onEditCluster: (Cluster) -> Unit,
    onDeleteCluster: (Cluster) -> Unit,
    onDeleteDeck: (Long) -> Unit,
    viewModel: AccountViewModel,
    snackbar: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var importText by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var showSignOut by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText().orEmpty()
                viewModel.importBackup(
                    json,
                    onDone = {
                        scope.launch { snackbar.showSnackbar("Backup restored") }
                        onDismiss()
                    },
                    onError = { scope.launch { snackbar.showSnackbar("Invalid backup") } },
                )
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
                onDismiss()
            } catch (_: Exception) {
                snackbar.showSnackbar("Export failed")
            }
        }
    }

    if (showSignOut) {
        ConfirmDialog(
            title = "Sign out?",
            message = "Your data stays on this device.",
            confirmLabel = "Sign out",
            onConfirm = {
                viewModel.signOut { showSignOut = false; onDismiss() }
            },
            onDismiss = { showSignOut = false },
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import shared deck") },
            text = {
                OutlinedTextField(
                    value = importText,
                    onValueChange = { importText = it },
                    label = { Text("Paste share JSON") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.importShare(
                        importText,
                        null,
                        onDone = {
                            importText = ""
                            showImportDialog = false
                            scope.launch { snackbar.showSnackbar("Deck imported") }
                            onDismiss()
                        },
                        onError = { scope.launch { snackbar.showSnackbar("Invalid share data") } },
                    )
                }) { Text("Import") }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("Cancel") }
            },
        )
    }

    ModalDrawerSheet(modifier = modifier) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 20.dp),
        ) {
            Text("Shine", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                if (state.session.isSignedIn) state.session.name else state.settings.userName.ifBlank { "Flashcards" },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (state.session.isSignedIn) {
                Text(
                    state.session.email.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    "${state.clusterCount} clusters · ${state.deckCount} decks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            DrawerClusterLibrary(
                state = homeState,
                onOpenDeck = onOpenDeck,
                onStudyDeck = onStudyDeck,
                onAddDeckToCluster = onAddDeckToCluster,
                onNewCluster = onNewCluster,
                onEditCluster = onEditCluster,
                onDeleteCluster = onDeleteCluster,
                onDeleteDeck = onDeleteDeck,
                onDismiss = onDismiss,
            )

            Spacer(Modifier.height(8.dp))
            Text(
                "Account",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp),
            )
            NavigationDrawerItem(
                label = { Text("Profile & sign in") },
                selected = selectedAction == DrawerAction.Account,
                onClick = { onAction(DrawerAction.Account) },
                icon = { Icon(Icons.Default.Person, contentDescription = null) },
            )
            NavigationDrawerItem(
                label = {
                    if (state.inbox.isNotEmpty()) {
                        BadgedBox(badge = { Badge { Text("${state.inbox.size}") } }) {
                            Text("Inbox")
                        }
                    } else {
                        Text("Inbox")
                    }
                },
                selected = selectedAction == DrawerAction.Inbox,
                onClick = { onAction(DrawerAction.Inbox) },
                icon = {
                    if (state.inbox.isNotEmpty()) {
                        BadgedBox(badge = { Badge { Text("${state.inbox.size}") } }) {
                            Icon(Icons.Default.Inbox, contentDescription = null)
                        }
                    } else {
                        Icon(Icons.Default.Inbox, contentDescription = null)
                    }
                },
            )

            Spacer(Modifier.height(8.dp))
            Text(
                "Backup",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp),
            )
            NavigationDrawerItem(
                label = { Text("Export backup") },
                selected = false,
                onClick = { exportLauncher.launch("shine-backup.json") },
                icon = { Icon(Icons.Default.Backup, contentDescription = null) },
            )
            NavigationDrawerItem(
                label = { Text("Import backup") },
                selected = false,
                onClick = { importLauncher.launch(arrayOf("application/json", "text/*")) },
                icon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
            )
            NavigationDrawerItem(
                label = { Text("Import shared deck") },
                selected = false,
                onClick = { showImportDialog = true },
                icon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
            )

            if (state.session.isSignedIn) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                NavigationDrawerItem(
                    label = { Text("Sign out") },
                    selected = false,
                    onClick = { showSignOut = true },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                )
            }
        }
    }
}
