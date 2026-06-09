package com.flashcards.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.flashcards.app.ui.components.ConfirmDialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flashcards.app.ui.components.FlipCard
import com.flashcards.app.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(viewModel: StudyViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    var showEndEarly by remember { mutableStateOf(false) }

    if (showEndEarly) {
        ConfirmDialog(
            title = "End session?",
            message = "Your progress so far will be saved.",
            confirmLabel = "End session",
            onConfirm = {
                viewModel.endEarly()
                showEndEarly = false
            },
            onDismiss = { showEndEarly = false },
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text(state.deck?.name ?: "Study") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    if (!state.isComplete && state.results.isNotEmpty()) {
                        TextButton(onClick = { showEndEarly = true }) {
                            Text("End", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when {
                state.isLoading -> Text("Loading cards…")
                state.cards.isEmpty() -> Text("No cards to study")
                state.isComplete -> StudyComplete(state.correctCount, state.incorrectCount, viewModel::restart, onBack)
                else -> {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(state.progressLabel, style = MaterialTheme.typography.labelLarge)
                        Text(if (state.isFlipped) "Answer" else "Question", color = MaterialTheme.colorScheme.primary)
                    }
                    LinearProgressIndicator(progress = { state.progress }, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))
                    FlipCard(
                        frontText = state.currentCard?.front.orEmpty(),
                        backText = state.currentCard?.back.orEmpty(),
                        isFlipped = state.isFlipped,
                        onFlip = viewModel::flipCard,
                        colorId = state.currentCard?.color,
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 16.dp),
                    )
                    Text(
                        if (state.isFlipped) "How did you do?" else "Tap the card to reveal",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                    if (state.isFlipped) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = viewModel::markIncorrect, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Close, null)
                                Text("Missed", modifier = Modifier.padding(start = 6.dp))
                            }
                            Button(onClick = viewModel::markCorrect, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Check, null)
                                Text("Got it", modifier = Modifier.padding(start = 6.dp))
                            }
                        }
                    } else {
                        OutlinedButton(onClick = viewModel::flipCard, modifier = Modifier.fillMaxWidth()) { Text("Show Answer") }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudyComplete(correct: Int, incorrect: Int, onRestart: () -> Unit, onBack: () -> Unit) {
    val total = correct + incorrect
    val percent = if (total == 0) 0 else correct * 100 / total
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.EmojiEvents, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(bottom = 16.dp))
        Text("Session Complete!", style = MaterialTheme.typography.headlineSmall)
        Text("$percent% correct", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 16.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(20.dp)) {
                Text("Got it: $correct")
                Text("Missed: $incorrect")
                Text("Total: $total")
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Refresh, null)
            Text("Study Again", modifier = Modifier.padding(start = 8.dp))
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Back to Deck") }
    }
}
