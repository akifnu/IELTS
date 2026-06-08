package com.flashcards.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flashcards.app.data.ShineRepository
import com.flashcards.app.viewmodel.AccountViewModel
import com.flashcards.app.viewmodel.CalendarViewModel
import com.flashcards.app.viewmodel.HomeViewModel

@Composable
fun MainTabsScreen(
    repository: ShineRepository,
    onOpenDeck: (Long) -> Unit,
    onStudyDeck: (Long) -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val homeVm: HomeViewModel = viewModel(factory = HomeViewModel.Factory(repository))
    val calendarVm: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory(repository))
    val accountVm: AccountViewModel = viewModel(factory = AccountViewModel.Factory(repository))

    Scaffold(
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
        when (tab) {
            0 -> HomeScreen(
                viewModel = homeVm,
                onOpenDeck = onOpenDeck,
                onStudyDeck = onStudyDeck,
                modifier = Modifier.padding(padding),
            )
            1 -> CalendarScreen(
                viewModel = calendarVm,
                onStudyDeck = onStudyDeck,
                modifier = Modifier.padding(padding),
            )
            2 -> AccountScreen(
                viewModel = accountVm,
                modifier = Modifier.padding(padding),
            )
        }
    }
}
