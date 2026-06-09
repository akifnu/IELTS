package com.flashcards.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flashcards.app.ui.components.GoogleSignInButton
import com.flashcards.app.util.GoogleSignInHelper
import com.flashcards.app.viewmodel.AccountViewModel
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(
    viewModel: AccountViewModel,
    snackbar: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var authMode by remember { mutableStateOf("signin") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
    ) {
        item {
            Card {
                Column(Modifier.padding(20.dp)) {
                    if (state.session.isSignedIn) {
                        Text(state.session.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text(
                            state.session.email.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Text(
                            "Signed in with ${state.session.provider}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        Text(
                            "Use the menu for backup, inbox, and sign out.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    } else {
                        Text("Welcome", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Sign in to sync your profile. Your decks always stay on this device.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Row(Modifier.padding(top = 16.dp)) {
                            TextButton(onClick = { authMode = "signin" }) {
                                Text(if (authMode == "signin") "• Sign in" else "Sign in")
                            }
                            TextButton(onClick = { authMode = "register" }) {
                                Text(if (authMode == "register") "• Register" else "Register")
                            }
                        }
                        if (authMode == "register") {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Name") },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                singleLine = true,
                            )
                        }
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password (8+ chars)") },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            singleLine = true,
                        )
                        state.authError?.let {
                            Text(
                                it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                        Button(
                            onClick = {
                                if (authMode == "register") {
                                    viewModel.registerEmail(name, email, password) {
                                        email = ""
                                        password = ""
                                        name = ""
                                        scope.launch { snackbar.showSnackbar("Account created") }
                                    }
                                } else {
                                    viewModel.signInEmail(email, password) {
                                        scope.launch { snackbar.showSnackbar("Signed in") }
                                    }
                                }
                            },
                            enabled = !state.authBusy,
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        ) { Text(if (authMode == "register") "Create account" else "Sign in") }
                        GoogleSignInButton(
                            onClick = {
                                scope.launch {
                                    val cred = GoogleSignInHelper.signIn(context)
                                    if (cred != null) {
                                        viewModel.signInGoogle(
                                            cred.id,
                                            null,
                                            cred.displayName,
                                            cred.profilePictureUri?.toString(),
                                        ) { scope.launch { snackbar.showSnackbar("Signed in with Google") } }
                                    } else {
                                        snackbar.showSnackbar("Google sign-in unavailable")
                                    }
                                }
                            },
                            modifier = Modifier.padding(top = 12.dp),
                            lightBackground = false,
                        )
                    }
                }
            }
        }
        item {
            Card {
                Column(Modifier.padding(20.dp)) {
                    Text("Your profile", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = state.settings.userName,
                        onValueChange = { viewModel.updateUserName(it) },
                        label = { Text("Display name") },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        singleLine = true,
                    )
                    Text(
                        "${state.clusterCount} clusters · ${state.deckCount} decks on this device",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        }
    }
}
