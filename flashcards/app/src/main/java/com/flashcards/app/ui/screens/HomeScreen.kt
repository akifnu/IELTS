package com.flashcards.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.flashcards.app.domain.Cluster
import com.flashcards.app.domain.DateUtils
import com.flashcards.app.domain.Deck
import com.flashcards.app.domain.DeckPermissions
import com.flashcards.app.domain.ShineConstants
import com.flashcards.app.domain.SpacedRepetitionEngine
import com.flashcards.app.ui.components.ConfirmDialog
import com.flashcards.app.ui.components.DeckDialog
import com.flashcards.app.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenDeck: (Long) -> Unit,
    onStudyDeck: (Long) -> Unit,
    showDeckDialog: Long?,
    onDismissDeckDialog: () -> Unit,
    onAddDeckToCluster: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    var showClusterDialog by remember { mutableStateOf(false) }
    var editCluster by remember { mutableStateOf<Cluster?>(null) }
    var deleteDeckId by remember { mutableStateOf<Long?>(null) }
    var deleteClusterTarget by remember { mutableStateOf<Cluster?>(null) }
    var collapsedClusters by remember { mutableStateOf(setOf<Long>()) }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.dueDecks.isNotEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("${state.dueDecks.size} deck(s) due today", fontWeight = FontWeight.Bold)
                        state.dueDecks.forEach { d ->
                            TextButton(onClick = { onStudyDeck(d.id) }) { Text("Study ${d.name}") }
                        }
                    }
                }
            }
        }
        state.clusters.forEach { cluster ->
            val decks = state.ownedDecks.filter { it.clusterId == cluster.id }
            item {
                ClusterSection(
                    cluster = cluster,
                    decks = decks,
                    onOpenDeck = onOpenDeck,
                    onStudyDeck = onStudyDeck,
                    onAddDeck = { onAddDeckToCluster(cluster.id) },
                    onEditCluster = { editCluster = cluster },
                    onDeleteCluster = { deleteClusterTarget = cluster },
                    onShareDeck = { onOpenDeck(it) },
                    onDeleteDeck = { deleteDeckId = it },
                    collapsed = cluster.id in collapsedClusters,
                    onToggleCollapse = {
                        collapsedClusters = if (cluster.id in collapsedClusters) {
                            collapsedClusters - cluster.id
                        } else {
                            collapsedClusters + cluster.id
                        }
                    },
                )
            }
        }
        val other = state.ownedDecks.filter { it.clusterId == null }
        if (other.isNotEmpty()) {
            item {
                ClusterSection(
                    cluster = Cluster(id = -1, name = "Other", emoji = "📋"),
                    decks = other,
                    showActions = false,
                    onOpenDeck = onOpenDeck,
                    onStudyDeck = onStudyDeck,
                    onAddDeck = {},
                    onEditCluster = {},
                    onDeleteCluster = {},
                    onShareDeck = { onOpenDeck(it) },
                    onDeleteDeck = { deleteDeckId = it },
                )
            }
        }
        if (state.sharedDecks.isNotEmpty()) {
            item {
                ClusterSection(
                    cluster = Cluster(id = -2, name = "Shared with me", emoji = "🤝"),
                    decks = state.sharedDecks,
                    showActions = false,
                    onOpenDeck = onOpenDeck,
                    onStudyDeck = onStudyDeck,
                    onAddDeck = {},
                    onEditCluster = {},
                    onDeleteCluster = {},
                    onShareDeck = {},
                    onDeleteDeck = { deleteDeckId = it },
                    deleteLabel = "Leave",
                )
            }
        }
        item {
            TextButton(onClick = { showClusterDialog = true }) { Text("+ New cluster") }
        }
    }

    showDeckDialog?.let { clusterId ->
        DeckDialog(
            title = "New Deck",
            confirmLabel = "Create",
            onDismiss = onDismissDeckDialog,
            onConfirm = { name, desc -> viewModel.createDeck(name, desc, clusterId) { onDismissDeckDialog() } },
        )
    }
    if (showClusterDialog || editCluster != null) {
        var name by remember(editCluster) { mutableStateOf(editCluster?.name ?: "") }
        var emoji by remember(editCluster) { mutableStateOf(editCluster?.emoji ?: "📁") }
        AlertDialog(
            onDismissRequest = { showClusterDialog = false; editCluster = null },
            title = { Text(if (editCluster != null) "Edit cluster" else "New cluster") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = emoji, onValueChange = { emoji = it }, label = { Text("Emoji") }, singleLine = true)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editCluster != null) viewModel.updateCluster(editCluster!!.copy(name = name, emoji = emoji)) {
                        editCluster = null
                    } else viewModel.createCluster(name, emoji) { showClusterDialog = false }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showClusterDialog = false; editCluster = null }) { Text("Cancel") }
            },
        )
    }
    deleteDeckId?.let { id ->
        val isShared = state.sharedDecks.any { it.id == id }
        ConfirmDialog(
            title = if (isShared) "Leave deck?" else "Delete deck?",
            message = if (isShared) "You will lose access to this shared deck." else "This deck and all its cards will be deleted.",
            confirmLabel = if (isShared) "Leave" else "Delete",
            onConfirm = {
                if (isShared) viewModel.leaveDeck(id) else viewModel.deleteDeck(id)
                deleteDeckId = null
            },
            onDismiss = { deleteDeckId = null },
        )
    }
    deleteClusterTarget?.let { cluster ->
        ConfirmDialog(
            title = "Delete cluster?",
            message = "Decks will move to Other.",
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.deleteCluster(cluster)
                deleteClusterTarget = null
            },
            onDismiss = { deleteClusterTarget = null },
        )
    }
}

@Composable
private fun ClusterSection(
    cluster: Cluster,
    decks: List<Deck>,
    showActions: Boolean = true,
    deleteLabel: String = "Delete",
    collapsed: Boolean = false,
    onToggleCollapse: (() -> Unit)? = null,
    onOpenDeck: (Long) -> Unit,
    onStudyDeck: (Long) -> Unit,
    onAddDeck: () -> Unit,
    onEditCluster: () -> Unit,
    onDeleteCluster: () -> Unit,
    onShareDeck: (Long) -> Unit,
    onDeleteDeck: (Long) -> Unit,
) {
    Card {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${if (collapsed) "▸" else "▾"} ${cluster.emoji} ${cluster.name}",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onToggleCollapse?.invoke() },
                )
                if (showActions && cluster.id > 0) {
                    Row {
                        IconButton(onClick = onAddDeck) { Icon(Icons.Default.Add, null) }
                        TextButton(onClick = onEditCluster) { Text("Edit") }
                        IconButton(onClick = onDeleteCluster) { Icon(Icons.Default.Delete, null) }
                    }
                }
            }
            if (!collapsed) {
                decks.forEach { deck ->
                    DeckRow(deck, onOpenDeck, onStudyDeck, onShareDeck, onDeleteDeck, deleteLabel)
                }
                if (decks.isEmpty()) Text("No decks yet", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@Composable
private fun DeckRow(
    deck: Deck,
    onOpenDeck: (Long) -> Unit,
    onStudyDeck: (Long) -> Unit,
    onShareDeck: (Long) -> Unit,
    onDeleteDeck: (Long) -> Unit,
    deleteLabel: String,
) {
    val due = SpacedRepetitionEngine.isDue(deck)
    val next = SpacedRepetitionEngine.nextScheduled(deck)
    val algo = if (DeckPermissions.isSmartScheduleOn(deck)) ShineConstants.ALGORITHMS[deck.algo.algorithm] else null
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onOpenDeck(deck.id) },
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(deck.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (deck.description.isNotBlank()) Text(deck.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                Text(
                    "${deck.cards.size} cards · ${if (due) "Due today" else next?.let { DateUtils.formatDate(it) } ?: "On track"}${algo?.let { " · $it" } ?: ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (due) TextButton(onClick = { onStudyDeck(deck.id) }) { Text("Study") }
            if (DeckPermissions.canShare(deck)) {
                IconButton(onClick = { onShareDeck(deck.id) }) { Icon(Icons.Default.Share, null) }
            }
            IconButton(onClick = { onDeleteDeck(deck.id) }) { Icon(Icons.Default.Delete, contentDescription = deleteLabel) }
        }
    }
}
