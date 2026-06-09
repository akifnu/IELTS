package com.flashcards.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flashcards.app.ui.theme.ShineAccent
import com.flashcards.app.ui.theme.ShinePrimary
import com.flashcards.app.ui.theme.ShinePrimaryDark
import com.flashcards.app.viewmodel.SplashViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onContinueGuest: () -> Unit,
    onSignedIn: () -> Unit,
    onNeedOnboarding: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.ready) {
        if (!state.ready) return@LaunchedEffect
        delay(1200)
        when {
            !state.onboarded -> onNeedOnboarding()
            else -> onSignedIn()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(ShinePrimary, ShinePrimaryDark, ShineAccent),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                "✨",
                style = MaterialTheme.typography.displayLarge,
            )
            Text(
                "Shine",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                if (state.session.name.isNotBlank() && state.session.name != "Guest") {
                    "Hi ${state.session.name}"
                } else {
                    "Your knowledge, organized"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(Modifier.height(48.dp))
            Button(
                onClick = {
                    viewModel.continueAsGuest()
                    if (state.onboarded) onSignedIn() else onNeedOnboarding()
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Continue") }
            OutlinedButton(
                onClick = {
                    viewModel.continueAsGuest()
                    if (state.onboarded) onSignedIn() else onNeedOnboarding()
                },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) { Text("Continue as guest") }
        }
    }
}
