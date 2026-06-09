package com.flashcards.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flashcards.app.viewmodel.AccountViewModel
import kotlinx.coroutines.launch

@Composable
fun InboxScreen(
    viewModel: AccountViewModel,
    snackbar: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    if (state.inbox.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Inbox is empty", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(
                "When someone shares a deck with you, it will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(state.inbox) { item ->
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Shared deck",
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "From ${item.fromName ?: item.fromEmail ?: "someone"}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Row(Modifier.padding(top = 12.dp)) {
                        Button(onClick = {
                            viewModel.acceptInbox(
                                item,
                                null,
                                onDone = { scope.launch { snackbar.showSnackbar("Deck added") } },
                                onError = { scope.launch { snackbar.showSnackbar("Import failed") } },
                            )
                        }) { Text("Accept") }
                        TextButton(onClick = { viewModel.dismissInbox(item.id) }) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }
    }
}
