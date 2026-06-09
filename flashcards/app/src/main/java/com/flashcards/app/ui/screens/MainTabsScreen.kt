package com.flashcards.app.ui.screens

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.flashcards.app.ui.components.DrawerAction
import com.flashcards.app.ui.components.ShineAppDrawer
import com.flashcards.app.viewmodel.AccountViewModel
import com.flashcards.app.viewmodel.CalendarViewModel
import com.flashcards.app.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

private enum class MainDestination {
    Decks,
    Calendar,
    Account,
    Inbox,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabsScreen(
    onOpenDeck: (Long) -> Unit,
    onStudyDeck: (Long) -> Unit,
    homeVm: HomeViewModel = hiltViewModel(),
    calendarVm: CalendarViewModel = hiltViewModel(),
    accountVm: AccountViewModel = hiltViewModel(),
) {
    var destination by rememberSaveable { mutableStateOf(MainDestination.Decks) }
    var showDeckDialog by remember { mutableStateOf<Long?>(null) }
    var requestNewCluster by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val homeState by homeVm.uiState.collectAsState()
    val accountState by accountVm.uiState.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navSuiteType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())

    val drawerAction = when (destination) {
        MainDestination.Account -> DrawerAction.Account
        MainDestination.Inbox -> DrawerAction.Inbox
        else -> null
    }

    fun navigateTo(dest: MainDestination) {
        destination = dest
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ShineAppDrawer(
                state = accountState,
                selectedAction = drawerAction,
                onAction = { action ->
                    when (action) {
                        DrawerAction.Account -> navigateTo(MainDestination.Account)
                        DrawerAction.Inbox -> navigateTo(MainDestination.Inbox)
                        else -> Unit
                    }
                },
                onDismiss = { scope.launch { drawerState.close() } },
                viewModel = accountVm,
                snackbar = snackbar,
            )
        },
    ) {
        NavigationSuiteScaffold(
            layoutType = navSuiteType,
            navigationSuiteItems = {
                item(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Decks") },
                    selected = destination == MainDestination.Decks,
                    onClick = { destination = MainDestination.Decks },
                )
                item(
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text("Calendar") },
                    selected = destination == MainDestination.Calendar,
                    onClick = { destination = MainDestination.Calendar },
                )
                item(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Account") },
                    selected = destination == MainDestination.Account,
                    onClick = { destination = MainDestination.Account },
                )
            },
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets.safeDrawing,
                topBar = {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Open menu")
                            }
                        },
                        title = {
                            when (destination) {
                                MainDestination.Decks -> Text("Decks")
                                MainDestination.Calendar -> Text("Study Calendar")
                                MainDestination.Account -> Text("Account")
                                MainDestination.Inbox -> Text("Inbox")
                            }
                        },
                        actions = {
                            if (destination == MainDestination.Decks) {
                                IconButton(onClick = { requestNewCluster = true }) {
                                    Icon(Icons.Default.CreateNewFolder, contentDescription = "New cluster")
                                }
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
                snackbarHost = { SnackbarHost(snackbar) },
                floatingActionButton = {
                    if (destination == MainDestination.Decks) {
                        FloatingActionButton(onClick = { showDeckDialog = homeState.clusters.firstOrNull()?.id }) {
                            Icon(Icons.Default.Add, contentDescription = "New deck")
                        }
                    }
                },
            ) { padding ->
                val contentModifier = Modifier.fillMaxSize().padding(padding)
                when (destination) {
                    MainDestination.Decks -> HomeScreen(
                        viewModel = homeVm,
                        onOpenDeck = onOpenDeck,
                        onStudyDeck = onStudyDeck,
                        showDeckDialog = showDeckDialog,
                        onDismissDeckDialog = { showDeckDialog = null },
                        onAddDeckToCluster = { showDeckDialog = it },
                        requestNewCluster = requestNewCluster,
                        onRequestNewClusterHandled = { requestNewCluster = false },
                        modifier = contentModifier,
                    )
                    MainDestination.Calendar -> CalendarScreen(
                        viewModel = calendarVm,
                        onStudyDeck = onStudyDeck,
                        modifier = contentModifier,
                    )
                    MainDestination.Account -> AccountScreen(
                        viewModel = accountVm,
                        snackbar = snackbar,
                        modifier = contentModifier,
                    )
                    MainDestination.Inbox -> InboxScreen(
                        viewModel = accountVm,
                        snackbar = snackbar,
                        modifier = contentModifier,
                    )
                }
            }
        }
    }
}
