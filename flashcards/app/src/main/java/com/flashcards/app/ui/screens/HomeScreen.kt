package com.flashcards.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.flashcards.app.domain.Cluster
import com.flashcards.app.domain.DateUtils
import com.flashcards.app.domain.Deck
import com.flashcards.app.domain.DeckPermissions
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
    requestNewCluster: Boolean = false,
    onRequestNewClusterHandled: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    var showClusterDialog by remember { mutableStateOf(false) }
    var editCluster by remember { mutableStateOf<Cluster?>(null) }
    var deleteDeckId by remember { mutableStateOf<Long?>(null) }
    var deleteClusterTarget by remember { mutableStateOf<Cluster?>(null) }
    var collapsedClusters by rememberSaveable { mutableStateOf(setOf<Long>()) }
    var initializedCollapse by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(requestNewCluster) {
        if (requestNewCluster) {
            showClusterDialog = true
            onRequestNewClusterHandled()
        }
    }

    LaunchedEffect(state.clusters, state.ownedDecks) {
        if (!initializedCollapse && state.clusters.isNotEmpty()) {
            collapsedClusters = state.clusters
                .filter { cluster ->
                    state.ownedDecks.none { it.clusterId == cluster.id }
                }
                .map { it.id }
                .toSet() + setOf(-2L)
            initializedCollapse = true
        }
    }

    if (state.clusters.isEmpty() && state.ownedDecks.isEmpty() && state.sharedDecks.isEmpty()) {
        EmptyHomeState(
            onCreateCluster = { showClusterDialog = true },
            modifier = modifier,
        )
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(bottom = 88.dp),
        ) {
            if (state.dueDecks.isNotEmpty()) {
                item(key = "due-hero") {
                    DueTodayHero(
                        dueDecks = state.dueDecks,
                        onStudyDeck = onStudyDeck,
                    )
                }
            }

            state.clusters.forEach { cluster ->
                val decks = state.ownedDecks.filter { it.clusterId == cluster.id }
                val expanded = cluster.id !in collapsedClusters
                item(key = "cluster-header-${cluster.id}") {
                    ClusterHeader(
                        cluster = cluster,
                        deckCount = decks.size,
                        expanded = expanded,
                        showActions = true,
                        onToggle = {
                            collapsedClusters = if (expanded) {
                                collapsedClusters + cluster.id
                            } else {
                                collapsedClusters - cluster.id
                            }
                        },
                        onAddDeck = { onAddDeckToCluster(cluster.id) },
                        onEditCluster = { editCluster = cluster },
                        onDeleteCluster = { deleteClusterTarget = cluster },
                    )
                }
                if (expanded) {
                    if (decks.isEmpty()) {
                        item(key = "cluster-empty-${cluster.id}") {
                            Text(
                                "No decks yet — tap + on the cluster menu to add one.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            )
                        }
                    } else {
                        items(decks, key = { "deck-${it.id}" }) { deck ->
                            DeckListItem(
                                deck = deck,
                                onOpenDeck = onOpenDeck,
                                onStudyDeck = onStudyDeck,
                                onShareDeck = { onOpenDeck(it) },
                                onDeleteDeck = { deleteDeckId = it },
                            )
                        }
                    }
                    item(key = "cluster-divider-${cluster.id}") {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                }
            }

            val other = state.ownedDecks.filter { it.clusterId == null }
            if (other.isNotEmpty()) {
                renderClusterGroup(
                    clusterId = -1L,
                    title = "Other",
                    emoji = "📋",
                    decks = other,
                    collapsedClusters = collapsedClusters,
                    onToggleCollapse = { id, expand ->
                        collapsedClusters = if (expand) collapsedClusters - id else collapsedClusters + id
                    },
                    showActions = false,
                    onOpenDeck = onOpenDeck,
                    onStudyDeck = onStudyDeck,
                    onShareDeck = { onOpenDeck(it) },
                    onDeleteDeck = { deleteDeckId = it },
                )
            }

            if (state.sharedDecks.isNotEmpty()) {
                renderClusterGroup(
                    clusterId = -2L,
                    title = "Shared with me",
                    emoji = "🤝",
                    decks = state.sharedDecks,
                    collapsedClusters = collapsedClusters,
                    onToggleCollapse = { id, expand ->
                        collapsedClusters = if (expand) collapsedClusters - id else collapsedClusters + id
                    },
                    showActions = false,
                    onOpenDeck = onOpenDeck,
                    onStudyDeck = onStudyDeck,
                    onShareDeck = {},
                    onDeleteDeck = { deleteDeckId = it },
                    deleteLabel = "Leave",
                )
            }
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
                    if (editCluster != null) {
                        viewModel.updateCluster(editCluster!!.copy(name = name, emoji = emoji)) {
                            editCluster = null
                        }
                    } else {
                        viewModel.createCluster(name, emoji) { showClusterDialog = false }
                    }
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

private fun LazyListScope.renderClusterGroup(
    clusterId: Long,
    title: String,
    emoji: String,
    decks: List<Deck>,
    collapsedClusters: Set<Long>,
    onToggleCollapse: (Long, Boolean) -> Unit,
    showActions: Boolean,
    onOpenDeck: (Long) -> Unit,
    onStudyDeck: (Long) -> Unit,
    onShareDeck: (Long) -> Unit,
    onDeleteDeck: (Long) -> Unit,
    deleteLabel: String = "Delete",
) {
    val expanded = clusterId !in collapsedClusters
    item(key = "cluster-header-$clusterId") {
        ClusterHeader(
            cluster = Cluster(id = clusterId, name = title, emoji = emoji),
            deckCount = decks.size,
            expanded = expanded,
            showActions = showActions,
            onToggle = { onToggleCollapse(clusterId, !expanded) },
            onAddDeck = {},
            onEditCluster = {},
            onDeleteCluster = {},
        )
    }
    if (expanded) {
        items(decks, key = { "deck-${it.id}" }) { deck ->
            DeckListItem(
                deck = deck,
                onOpenDeck = onOpenDeck,
                onStudyDeck = onStudyDeck,
                onShareDeck = onShareDeck,
                onDeleteDeck = onDeleteDeck,
                deleteLabel = deleteLabel,
            )
        }
        item(key = "cluster-divider-$clusterId") {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}

@Composable
private fun EmptyHomeState(
    onCreateCluster: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("📁", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(16.dp))
        Text("Organize your decks", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(
            "Create a cluster to group related decks — like IELTS, work, or languages.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        Button(onClick = onCreateCluster, modifier = Modifier.padding(top = 24.dp)) {
            Text("Create your first cluster")
        }
    }
}

@Composable
private fun DueTodayHero(
    dueDecks: List<Deck>,
    onStudyDeck: (Long) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                if (dueDecks.size == 1) "1 deck due today" else "${dueDecks.size} decks due today",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                "Ready when you are — pick a deck to review.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp),
            )
            if (dueDecks.size == 1) {
                Button(
                    onClick = { onStudyDeck(dueDecks.first().id) },
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    Text("Study ${dueDecks.first().name}")
                }
            } else {
                LazyRow(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(dueDecks, key = { it.id }) { deck ->
                        SuggestionChip(
                            onClick = { onStudyDeck(deck.id) },
                            label = { Text(deck.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClusterHeader(
    cluster: Cluster,
    deckCount: Int,
    expanded: Boolean,
    showActions: Boolean,
    onToggle: () -> Unit,
    onAddDeck: () -> Unit,
    onEditCluster: () -> Unit,
    onDeleteCluster: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(start = 8.dp, end = 4.dp, top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            cluster.emoji,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(
                cluster.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "$deckCount deck${if (deckCount == 1) "" else "s"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (showActions && cluster.id > 0) {
            ClusterActionsMenu(
                onAddDeck = onAddDeck,
                onEditCluster = onEditCluster,
                onDeleteCluster = onDeleteCluster,
            )
        }
        Icon(
            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = if (expanded) "Collapse" else "Expand",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp),
        )
    }
}

@Composable
private fun ClusterActionsMenu(
    onAddDeck: () -> Unit,
    onEditCluster: () -> Unit,
    onDeleteCluster: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Default.MoreVert, contentDescription = "Cluster options")
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            text = { Text("Add deck") },
            onClick = { expanded = false; onAddDeck() },
            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
        )
        DropdownMenuItem(
            text = { Text("Edit cluster") },
            onClick = { expanded = false; onEditCluster() },
        )
        DropdownMenuItem(
            text = { Text("Delete cluster") },
            onClick = { expanded = false; onDeleteCluster() },
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
        )
    }
}

@Composable
private fun DeckListItem(
    deck: Deck,
    onOpenDeck: (Long) -> Unit,
    onStudyDeck: (Long) -> Unit,
    onShareDeck: (Long) -> Unit,
    onDeleteDeck: (Long) -> Unit,
    deleteLabel: String = "Delete",
) {
    val due = SpacedRepetitionEngine.isDue(deck)
    val next = SpacedRepetitionEngine.nextScheduled(deck)
    var menuOpen by remember { mutableStateOf(false) }

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDeck(deck.id) },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        leadingContent = {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (due) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    ),
            )
        },
        headlineContent = {
            Text(
                deck.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text(
                buildString {
                    append("${deck.cards.size} cards")
                    append(" · ")
                    append(if (due) "Due today" else next?.let { DateUtils.formatDate(it) } ?: "On track")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Deck options")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    if (due) {
                        DropdownMenuItem(
                            text = { Text("Study now") },
                            onClick = { menuOpen = false; onStudyDeck(deck.id) },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Open deck") },
                        onClick = { menuOpen = false; onOpenDeck(deck.id) },
                    )
                    if (DeckPermissions.canShare(deck)) {
                        DropdownMenuItem(
                            text = { Text("Share") },
                            onClick = { menuOpen = false; onShareDeck(deck.id) },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(deleteLabel) },
                        onClick = { menuOpen = false; onDeleteDeck(deck.id) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    )
                }
            }
        },
    )
}
