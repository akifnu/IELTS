package com.flashcards.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flashcards.app.domain.Cluster
import com.flashcards.app.ui.components.ConfirmDialog
import com.flashcards.app.ui.components.DeckDialog
import com.flashcards.app.ui.components.DrawerAction
import com.flashcards.app.ui.components.ShineAppDrawer
import com.flashcards.app.viewmodel.AccountViewModel
import com.flashcards.app.viewmodel.CalendarViewModel
import com.flashcards.app.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

private enum class MainDestination {
    Today,
    Calendar,
    Clusters,
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
    var destination by rememberSaveable { mutableStateOf(MainDestination.Today) }
    var showDeckDialog by remember { mutableStateOf<Long?>(null) }
    var showClusterDialog by remember { mutableStateOf(false) }
    var editCluster by remember { mutableStateOf<Cluster?>(null) }
    var deleteDeckId by remember { mutableStateOf<Long?>(null) }
    var deleteClusterTarget by remember { mutableStateOf<Cluster?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val homeState by homeVm.uiState.collectAsState()
    val accountState by accountVm.uiState.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navSuiteType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())

    val drawerAction = when (destination) {
        MainDestination.Clusters -> DrawerAction.Clusters
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
                        DrawerAction.Clusters -> navigateTo(MainDestination.Clusters)
                        DrawerAction.Account -> navigateTo(MainDestination.Account)
                        DrawerAction.Inbox -> navigateTo(MainDestination.Inbox)
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
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Today") },
                    selected = destination == MainDestination.Today,
                    onClick = { destination = MainDestination.Today },
                )
                item(
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text("Calendar") },
                    selected = destination == MainDestination.Calendar,
                    onClick = { destination = MainDestination.Calendar },
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
                                MainDestination.Today -> Text("Today")
                                MainDestination.Calendar -> Text("Study Calendar")
                                MainDestination.Clusters -> Text("Clusters")
                                MainDestination.Account -> Text("Account")
                                MainDestination.Inbox -> Text("Inbox")
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
                    if (destination == MainDestination.Clusters) {
                        FloatingActionButton(onClick = { showClusterDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "New cluster")
                        }
                    }
                },
            ) { padding ->
                val contentModifier = Modifier.fillMaxSize().padding(padding)
                when (destination) {
                    MainDestination.Today -> HomeScreen(
                        viewModel = homeVm,
                        onStudyDeck = onStudyDeck,
                        modifier = contentModifier,
                    )
                    MainDestination.Calendar -> CalendarScreen(
                        viewModel = calendarVm,
                        onStudyDeck = onStudyDeck,
                        modifier = contentModifier,
                    )
                    MainDestination.Clusters -> LibraryScreen(
                        viewModel = homeVm,
                        onOpenDeck = onOpenDeck,
                        onStudyDeck = onStudyDeck,
                        onAddDeckToCluster = { showDeckDialog = it },
                        onNewCluster = { showClusterDialog = true },
                        onEditCluster = { editCluster = it },
                        onDeleteCluster = { deleteClusterTarget = it },
                        onDeleteDeck = { deleteDeckId = it },
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

    showDeckDialog?.let { clusterId ->
        DeckDialog(
            title = "New Deck",
            confirmLabel = "Create",
            onDismiss = { showDeckDialog = null },
            onConfirm = { name, desc -> homeVm.createDeck(name, desc, clusterId) { showDeckDialog = null } },
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
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editCluster != null) {
                        homeVm.updateCluster(editCluster!!.copy(name = name, emoji = emoji)) {
                            editCluster = null
                        }
                    } else {
                        homeVm.createCluster(name, emoji) {
                            showClusterDialog = false
                            destination = MainDestination.Clusters
                        }
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showClusterDialog = false; editCluster = null }) { Text("Cancel") }
            },
        )
    }

    deleteDeckId?.let { id ->
        val isShared = homeState.sharedDecks.any { it.id == id }
        ConfirmDialog(
            title = if (isShared) "Leave deck?" else "Delete deck?",
            message = if (isShared) "You will lose access to this shared deck." else "This deck and all its cards will be deleted.",
            confirmLabel = if (isShared) "Leave" else "Delete",
            onConfirm = {
                if (isShared) homeVm.leaveDeck(id) else homeVm.deleteDeck(id)
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
                homeVm.deleteCluster(cluster)
                deleteClusterTarget = null
            },
            onDismiss = { deleteClusterTarget = null },
        )
    }
}
