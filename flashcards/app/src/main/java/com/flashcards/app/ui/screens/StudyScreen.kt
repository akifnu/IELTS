package com.flashcards.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flashcards.app.ui.components.FlipCard
import com.flashcards.app.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    viewModel: StudyViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study Session") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    Text(
                        text = "Loading cards…",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.cards.isEmpty() -> {
                    EmptyStudyState(onBack = onBack, modifier = Modifier.align(Alignment.Center))
                }
                state.isComplete -> {
                    StudyCompleteState(
                        correct = state.correctCount,
                        incorrect = state.incorrectCount,
                        onRestart = viewModel::restart,
                        onBack = onBack,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    StudySession(
                        front = state.currentCard?.front.orEmpty(),
                        back = state.currentCard?.back.orEmpty(),
                        isFlipped = state.isFlipped,
                        progress = state.progress,
                        progressLabel = state.progressLabel,
                        onFlip = viewModel::flipCard,
                        onCorrect = viewModel::markCorrect,
                        onIncorrect = viewModel::markIncorrect
                    )
                }
            }
        }
    }
}

@Composable
private fun StudySession(
    front: String,
    back: String,
    isFlipped: Boolean,
    progress: Float,
    progressLabel: String,
    onFlip: () -> Unit,
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = progressLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (isFlipped) "Answer" else "Question",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )

        FlipCard(
            frontText = front,
            backText = back,
            isFlipped = isFlipped,
            onFlip = onFlip,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp)
        )

        Text(
            text = if (isFlipped) "How did you do?" else "Tap the card to reveal the answer",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isFlipped) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onIncorrect,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Text("Missed", modifier = Modifier.padding(start = 6.dp))
                }
                Button(
                    onClick = onCorrect,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Text("Got it", modifier = Modifier.padding(start = 6.dp))
                }
            }
        } else {
            OutlinedButton(
                onClick = onFlip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Show Answer")
            }
        }
    }
}

@Composable
private fun StudyCompleteState(
    correct: Int,
    incorrect: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = correct + incorrect
    val percent = if (total == 0) 0 else (correct * 100 / total)

    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Session Complete!",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "$percent% correct",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Got it: $correct")
                Text("Missed: $incorrect", modifier = Modifier.padding(top = 4.dp))
                Text("Total: $total", modifier = Modifier.padding(top = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Text("Study Again", modifier = Modifier.padding(start = 8.dp))
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Back to Deck")
        }
    }
}

@Composable
private fun EmptyStudyState(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No cards to study",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Add some cards to this deck first",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        Button(onClick = onBack) {
            Text("Go Back")
        }
    }
}
