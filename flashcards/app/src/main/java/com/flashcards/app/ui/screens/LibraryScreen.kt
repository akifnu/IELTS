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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.flashcards.app.domain.Cluster
import com.flashcards.app.domain.Deck
import com.flashcards.app.domain.DeckPermissions
import com.flashcards.app.viewmodel.HomeViewModel

@Composable
fun LibraryScreen(
    viewModel: HomeViewModel,
    onOpenDeck: (Long) -> Unit,
    onStudyDeck: (Long) -> Unit,
    onAddDeckToCluster: (Long) -> Unit,
    onNewCluster: () -> Unit,
    onEditCluster: (Cluster) -> Unit,
    onDeleteCluster: (Cluster) -> Unit,
    onDeleteDeck: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    var expandedClusters by rememberSaveable { mutableStateOf(setOf<Long>()) }

    if (state.clusters.isEmpty() && state.ownedDecks.isEmpty() && state.sharedDecks.isEmpty()) {
            Column(
                Modifier.fillMaxSize().then(modifier).padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("📁", style = MaterialTheme.typography.displaySmall)
                Text(
                    "No clusters yet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Text(
                    "Organize your decks into clusters — tap + to create one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
                TextButton(onClick = onNewCluster, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Create cluster")
                }
            }
            return
        }

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
        ) {
            state.clusters.forEach { cluster ->
                val decks = state.ownedDecks.filter { it.clusterId == cluster.id }
                val expanded = cluster.id in expandedClusters
                item(key = "header-${cluster.id}") {
                    ClusterHeaderRow(
                        cluster = cluster,
                        deckCount = decks.size,
                        expanded = expanded,
                        showActions = true,
                        onToggle = {
                            expandedClusters = if (expanded) expandedClusters - cluster.id else expandedClusters + cluster.id
                        },
                        onAddDeck = { onAddDeckToCluster(cluster.id) },
                        onEditCluster = { onEditCluster(cluster) },
                        onDeleteCluster = { onDeleteCluster(cluster) },
                    )
                }
                if (expanded) {
                    if (decks.isEmpty()) {
                        item(key = "empty-${cluster.id}") {
                            Text(
                                "No decks — tap Add deck on the cluster row.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            )
                        }
                    } else {
                        items(decks, key = { "deck-${it.id}" }) { deck ->
                            DeckLibraryRow(
                                deck = deck,
                                onOpen = { onOpenDeck(deck.id) },
                                onStudy = { onStudyDeck(deck.id) },
                                onDelete = { onDeleteDeck(deck.id) },
                            )
                        }
                    }
                    item(key = "divider-${cluster.id}") {
                        HorizontalDivider(Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                    }
                }
            }

            val other = state.ownedDecks.filter { it.clusterId == null }
            if (other.isNotEmpty()) {
                renderLibraryGroup(
                    clusterId = -1L,
                    title = "Other",
                    emoji = "📋",
                    decks = other,
                    expandedClusters = expandedClusters,
                    onToggle = { id, exp ->
                        expandedClusters = if (exp) expandedClusters + id else expandedClusters - id
                    },
                    onOpenDeck = onOpenDeck,
                    onStudyDeck = onStudyDeck,
                    onDeleteDeck = onDeleteDeck,
                )
            }

            if (state.sharedDecks.isNotEmpty()) {
                renderLibraryGroup(
                    clusterId = -2L,
                    title = "Shared with me",
                    emoji = "🤝",
                    decks = state.sharedDecks,
                    expandedClusters = expandedClusters,
                    onToggle = { id, exp ->
                        expandedClusters = if (exp) expandedClusters + id else expandedClusters - id
                    },
                    onOpenDeck = onOpenDeck,
                    onStudyDeck = onStudyDeck,
                    onDeleteDeck = onDeleteDeck,
                    deleteLabel = "Leave",
                )
            }
        }
}

private fun LazyListScope.renderLibraryGroup(
    clusterId: Long,
    title: String,
    emoji: String,
    decks: List<Deck>,
    expandedClusters: Set<Long>,
    onToggle: (Long, Boolean) -> Unit,
    onOpenDeck: (Long) -> Unit,
    onStudyDeck: (Long) -> Unit,
    onDeleteDeck: (Long) -> Unit,
    deleteLabel: String = "Delete",
) {
    val expanded = clusterId in expandedClusters
    item(key = "header-$clusterId") {
        ClusterHeaderRow(
            cluster = Cluster(id = clusterId, name = title, emoji = emoji),
            deckCount = decks.size,
            expanded = expanded,
            showActions = false,
            onToggle = { onToggle(clusterId, !expanded) },
            onAddDeck = {},
            onEditCluster = {},
            onDeleteCluster = {},
        )
    }
    if (expanded) {
        items(decks, key = { "deck-${it.id}" }) { deck ->
            DeckLibraryRow(
                deck = deck,
                onOpen = { onOpenDeck(deck.id) },
                onStudy = { onStudyDeck(deck.id) },
                onDelete = { onDeleteDeck(deck.id) },
                deleteLabel = deleteLabel,
            )
        }
        item(key = "divider-$clusterId") {
            HorizontalDivider(Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
        }
    }
}

@Composable
private fun ClusterHeaderRow(
    cluster: Cluster,
    deckCount: Int,
    expanded: Boolean,
    showActions: Boolean,
    onToggle: () -> Unit,
    onAddDeck: () -> Unit,
    onEditCluster: () -> Unit,
    onDeleteCluster: () -> Unit,
) {
    var menuOpen by rememberSaveable { mutableStateOf(false) }

    ListItem(
        modifier = Modifier.clickable(onClick = onToggle),
        headlineContent = {
            Text(
                "${cluster.emoji} ${cluster.name}",
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text("$deckCount deck${if (deckCount == 1) "" else "s"}")
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showActions && cluster.id > 0) {
                    TextButton(onClick = onAddDeck) { Text("Add deck") }
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Cluster options")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit cluster") },
                            onClick = { menuOpen = false; onEditCluster() },
                        )
                        DropdownMenuItem(
                            text = { Text("Delete cluster") },
                            onClick = { menuOpen = false; onDeleteCluster() },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        )
                    }
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

@Composable
private fun DeckLibraryRow(
    deck: Deck,
    onOpen: () -> Unit,
    onStudy: () -> Unit,
    onDelete: () -> Unit,
    deleteLabel: String = "Delete",
) {
    var menuOpen by rememberSaveable { mutableStateOf(false) }

    ListItem(
        modifier = Modifier
            .padding(start = 16.dp)
            .clickable(onClick = onOpen),
        headlineContent = {
            Text(deck.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Text("${deck.cards.size} cards")
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onStudy) { Text("Study") }
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Deck options")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(text = { Text("Open") }, onClick = { menuOpen = false; onOpen() })
                    if (DeckPermissions.canShare(deck)) {
                        DropdownMenuItem(
                            text = { Text("Share") },
                            onClick = { menuOpen = false; onOpen() },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(deleteLabel) },
                        onClick = { menuOpen = false; onDelete() },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    )
                }
            }
        },
    )
}
