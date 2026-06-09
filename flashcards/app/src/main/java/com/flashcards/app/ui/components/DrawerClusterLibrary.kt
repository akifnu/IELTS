package com.flashcards.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.flashcards.app.viewmodel.HomeUiState

@Composable
fun DrawerClusterLibrary(
    state: HomeUiState,
    onOpenDeck: (Long) -> Unit,
    onStudyDeck: (Long) -> Unit,
    onAddDeckToCluster: (Long) -> Unit,
    onNewCluster: () -> Unit,
    onEditCluster: (Cluster) -> Unit,
    onDeleteCluster: (Cluster) -> Unit,
    onDeleteDeck: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expandedClusters by rememberSaveable { mutableStateOf(setOf<Long>()) }

    Column(modifier) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Your clusters",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f).padding(start = 12.dp),
            )
            TextButton(onClick = onNewCluster) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                Text("New")
            }
        }

        if (state.clusters.isEmpty() && state.ownedDecks.isEmpty()) {
            Text(
                "Create a cluster to organize your decks.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
            )
        }

        state.clusters.forEach { cluster ->
            val decks = state.ownedDecks.filter { it.clusterId == cluster.id }
            DrawerClusterGroup(
                cluster = cluster,
                decks = decks,
                expanded = cluster.id in expandedClusters,
                onToggle = {
                    expandedClusters = if (cluster.id in expandedClusters) {
                        expandedClusters - cluster.id
                    } else {
                        expandedClusters + cluster.id
                    }
                },
                onAddDeck = { onAddDeckToCluster(cluster.id) },
                onEditCluster = { onEditCluster(cluster) },
                onDeleteCluster = { onDeleteCluster(cluster) },
                onOpenDeck = { onOpenDeck(it); onDismiss() },
                onStudyDeck = { onStudyDeck(it); onDismiss() },
                onDeleteDeck = onDeleteDeck,
            )
        }

        val other = state.ownedDecks.filter { it.clusterId == null }
        if (other.isNotEmpty()) {
            DrawerClusterGroup(
                cluster = Cluster(id = -1, name = "Other", emoji = "📋"),
                decks = other,
                expanded = -1L in expandedClusters,
                onToggle = {
                    expandedClusters = if (-1L in expandedClusters) expandedClusters - (-1L) else expandedClusters + (-1L)
                },
                onAddDeck = {},
                onEditCluster = {},
                onDeleteCluster = {},
                onOpenDeck = { onOpenDeck(it); onDismiss() },
                onStudyDeck = { onStudyDeck(it); onDismiss() },
                onDeleteDeck = onDeleteDeck,
                showClusterActions = false,
            )
        }

        if (state.sharedDecks.isNotEmpty()) {
            DrawerClusterGroup(
                cluster = Cluster(id = -2, name = "Shared with me", emoji = "🤝"),
                decks = state.sharedDecks,
                expanded = -2L in expandedClusters,
                onToggle = {
                    expandedClusters = if (-2L in expandedClusters) expandedClusters - (-2L) else expandedClusters + (-2L)
                },
                onAddDeck = {},
                onEditCluster = {},
                onDeleteCluster = {},
                onOpenDeck = { onOpenDeck(it); onDismiss() },
                onStudyDeck = { onStudyDeck(it); onDismiss() },
                onDeleteDeck = onDeleteDeck,
                showClusterActions = false,
                deleteLabel = "Leave",
            )
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
    }
}

@Composable
private fun DrawerClusterGroup(
    cluster: Cluster,
    decks: List<Deck>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onAddDeck: () -> Unit,
    onEditCluster: () -> Unit,
    onDeleteCluster: () -> Unit,
    onOpenDeck: (Long) -> Unit,
    onStudyDeck: (Long) -> Unit,
    onDeleteDeck: (Long) -> Unit,
    showClusterActions: Boolean = true,
    deleteLabel: String = "Delete",
) {
    var clusterMenuOpen by rememberSaveable { mutableStateOf(false) }

    NavigationDrawerItem(
        label = {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${cluster.emoji} ${cluster.name}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${decks.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        selected = false,
        onClick = onToggle,
        icon = {
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
            )
        },
        modifier = Modifier.padding(horizontal = 8.dp),
    )

    if (showClusterActions && cluster.id > 0) {
        Row(Modifier.fillMaxWidth().padding(start = 56.dp, end = 16.dp)) {
            TextButton(onClick = onAddDeck) { Text("Add deck") }
            IconButton(onClick = { clusterMenuOpen = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Cluster options")
            }
            DropdownMenu(expanded = clusterMenuOpen, onDismissRequest = { clusterMenuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Edit cluster") },
                    onClick = { clusterMenuOpen = false; onEditCluster() },
                )
                DropdownMenuItem(
                    text = { Text("Delete cluster") },
                    onClick = { clusterMenuOpen = false; onDeleteCluster() },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                )
            }
        }
    }

    if (expanded) {
        if (decks.isEmpty()) {
            Text(
                "No decks yet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 56.dp, bottom = 8.dp),
            )
        } else {
            decks.forEach { deck ->
                DrawerDeckItem(
                    deck = deck,
                    onOpen = { onOpenDeck(deck.id) },
                    onStudy = { onStudyDeck(deck.id) },
                    onDelete = { onDeleteDeck(deck.id) },
                    deleteLabel = deleteLabel,
                )
            }
        }
    }
}

@Composable
private fun DrawerDeckItem(
    deck: Deck,
    onOpen: () -> Unit,
    onStudy: () -> Unit,
    onDelete: () -> Unit,
    deleteLabel: String,
) {
    var menuOpen by rememberSaveable { mutableStateOf(false) }

    Row(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavigationDrawerItem(
            label = {
                Text(
                    deck.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            selected = false,
            onClick = onOpen,
            modifier = Modifier.weight(1f),
        )
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Deck options")
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(text = { Text("Study") }, onClick = { menuOpen = false; onStudy() })
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
    }
}
