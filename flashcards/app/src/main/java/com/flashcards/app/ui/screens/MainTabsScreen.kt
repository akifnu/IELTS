package com.flashcards.app.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flashcards.app.data.ShineRepository
import com.flashcards.app.viewmodel.AccountViewModel
import com.flashcards.app.viewmodel.CalendarViewModel
import com.flashcards.app.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabsScreen(
    repository: ShineRepository,
    onOpenDeck: (Long) -> Unit,
    onStudyDeck: (Long) -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showDeckDialog by remember { mutableStateOf<Long?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val homeVm: HomeViewModel = viewModel(factory = HomeViewModel.Factory(repository))
    val calendarVm: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory(repository))
    val accountVm: AccountViewModel = viewModel(factory = AccountViewModel.Factory(repository))
    val homeState by homeVm.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            when (tab) {
                0 -> TopAppBar(
                    title = {
                        androidx.compose.foundation.layout.Column {
                            Text("Shine")
                            Text("Your knowledge, organized", style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
                1 -> TopAppBar(
                    title = { Text("Study Calendar") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
                else -> TopAppBar(
                    title = { Text("Account") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            if (tab == 0) {
                FloatingActionButton(onClick = { showDeckDialog = homeState.clusters.firstOrNull()?.id }) {
                    Icon(Icons.Default.Add, contentDescription = "New deck")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Decks") },
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text("Calendar") },
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Account") },
                )
            }
        },
    ) { padding ->
        val contentModifier = Modifier.fillMaxSize().padding(padding)
        when (tab) {
            0 -> HomeScreen(
                viewModel = homeVm,
                onOpenDeck = onOpenDeck,
                onStudyDeck = onStudyDeck,
                showDeckDialog = showDeckDialog,
                onDismissDeckDialog = { showDeckDialog = null },
                onAddDeckToCluster = { showDeckDialog = it },
                modifier = contentModifier,
            )
            1 -> CalendarScreen(
                viewModel = calendarVm,
                onStudyDeck = onStudyDeck,
                modifier = contentModifier,
            )
            2 -> AccountScreen(
                viewModel = accountVm,
                snackbar = snackbar,
                modifier = contentModifier,
            )
        }
    }
}
