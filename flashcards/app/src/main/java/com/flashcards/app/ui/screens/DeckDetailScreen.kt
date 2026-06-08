package com.flashcards.app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flashcards.app.domain.DateUtils
import com.flashcards.app.domain.DeckPermissions
import com.flashcards.app.domain.Flashcard
import com.flashcards.app.domain.ShineConstants
import com.flashcards.app.domain.SpacedRepetitionEngine
import com.flashcards.app.ui.components.CardDialog
import com.flashcards.app.util.ShareHelper
import com.flashcards.app.viewmodel.DeckDetailViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(
    viewModel: DeckDetailViewModel,
    onBack: () -> Unit,
    onStudy: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val deck = state.deck
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showAddCard by remember { mutableStateOf(false) }
    var editCard by remember { mutableStateOf<Flashcard?>(null) }
    var showShare by remember { mutableStateOf(false) }
    var inviteEmail by remember { mutableStateOf("") }
    var inviteRole by remember { mutableStateOf("viewer") }

    if (showShare && deck != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        androidx.compose.material3.ModalBottomSheet(onDismissRequest = { showShare = false }, sheetState = sheetState) {
            Column(Modifier.padding(16.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Share ${deck.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                ) {
                    FilterChip(selected = inviteRole == "viewer", onClick = { inviteRole = "viewer" }, label = { Text("Viewer") })
                    FilterChip(selected = inviteRole == "editor", onClick = { inviteRole = "editor" }, label = { Text("Editor") })
                }
                OutlinedTextField(
                    value = inviteEmail,
                    onValueChange = { inviteEmail = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Button(
                    onClick = {
                        viewModel.addCollaborator(inviteEmail, inviteRole) { inviteEmail = "" }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Add collaborator") }
                deck.sharing.collaborators.forEach { c ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(c.email)
                            Text(c.role, style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(onClick = { viewModel.removeCollaborator(c.email) }) { Icon(Icons.Default.Delete, null) }
                    }
                }
                Button(
                    onClick = {
                        scope.launch {
                            val json = viewModel.buildShareJson(inviteRole)
                            ShareHelper.shareJson(context, "${deck.name}.json", json, "Shine: ${deck.name}")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Send file via Messages or Mail") }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(deck?.name ?: "Deck") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    if (state.canShare) {
                        IconButton(onClick = { showShare = true }) { Icon(Icons.Default.Share, null) }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        floatingActionButton = {
            if (state.canEdit) {
                FloatingActionButton(onClick = { showAddCard = true }) { Icon(Icons.Default.Add, null) }
            }
        },
    ) { padding ->
        if (deck == null) {
            Text("Loading…", Modifier.padding(padding).padding(24.dp))
            return@Scaffold
        }
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (deck.access != null) {
                item {
                    Card {
                        Text(
                            if (deck.access.role == "viewer") "View only — shared by ${deck.access.ownerName ?: "someone"}"
                            else "Can edit — shared by ${deck.access.ownerName ?: "someone"}",
                            Modifier.padding(12.dp),
                        )
                    }
                }
            }
            item {
                if (deck.cards.isNotEmpty()) {
                    Button(onClick = onStudy, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.PlayArrow, null)
                        Text(" Study (${deck.cards.size} cards)", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
            if (DeckPermissions.canChangeSettings(deck)) {
                item {
                    Card {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Smart schedule")
                                Switch(checked = state.smartScheduleOn, onCheckedChange = { viewModel.setScheduleMode(it) })
                            }
                            if (state.smartScheduleOn) {
                                Text("Algorithm", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                ) {
                                    ShineConstants.ALGORITHMS.keys.forEach { key ->
                                        FilterChip(
                                            selected = deck.algo.algorithm == key,
                                            onClick = { viewModel.setAlgorithm(key) },
                                            label = { Text(ShineConstants.ALGORITHMS[key] ?: key) },
                                        )
                                    }
                                }
                                Text("Preset", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                ) {
                                    listOf("relaxed", "normal", "intensive").forEach { p ->
                                        FilterChip(
                                            selected = deck.algo.preset == p,
                                            onClick = { viewModel.applyPreset(p) },
                                            label = { Text(p.replaceFirstChar { it.uppercase() }) },
                                        )
                                    }
                                }
                                Text(
                                    "Next: ${state.nextScheduled?.let { DateUtils.formatDate(it) } ?: "Not scheduled"}${if (state.isDue) " · Due today" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                        }
                    }
                }
            }
            items(deck.cards, key = { it.id }) { card ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(card.front, fontWeight = FontWeight.Bold)
                        Text(card.back, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
                        if (state.smartScheduleOn && deck.algo.algorithm == "leitner") {
                            Text("Leitner box ${card.leitnerBox}", style = MaterialTheme.typography.labelSmall)
                        }
                        if (state.canEdit) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                IconButton(onClick = { editCard = card }) { Icon(Icons.Default.Edit, null) }
                                IconButton(onClick = { viewModel.deleteCard(card) }) { Icon(Icons.Default.Delete, null) }
                            }
                        }
                    }
                }
            }
            if (DeckPermissions.canLeave(deck)) {
                item {
                    TextButton(onClick = { viewModel.leaveDeck(onBack) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Leave shared deck", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (showAddCard) {
        CardDialog(title = "New Card", confirmLabel = "Add", onDismiss = { showAddCard = false }, onConfirm = { f, b ->
            viewModel.addCard(f, b, null) { showAddCard = false }
        })
    }
    editCard?.let { card ->
        CardDialog(
            title = "Edit Card",
            initialFront = card.front,
            initialBack = card.back,
            onDismiss = { editCard = null },
            onConfirm = { f, b -> viewModel.updateCard(card, f, b, card.color) { editCard = null } },
        )
    }
}
