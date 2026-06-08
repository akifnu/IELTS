package com.flashcards.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.flashcards.app.FlashcardsApp
import com.flashcards.app.ui.screens.DeckDetailScreen
import com.flashcards.app.ui.screens.MainTabsScreen
import com.flashcards.app.ui.screens.StudyScreen
import com.flashcards.app.viewmodel.DeckDetailViewModel
import com.flashcards.app.viewmodel.StudyViewModel

@Composable
fun ShineNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val repository = (LocalContext.current.applicationContext as FlashcardsApp).repository

    NavHost(
        navController = navController,
        startDestination = NavRoutes.MAIN,
        modifier = modifier,
    ) {
        composable(NavRoutes.MAIN) {
            MainTabsScreen(
                repository = repository,
                onOpenDeck = { navController.navigate(NavRoutes.deckDetail(it)) },
                onStudyDeck = { navController.navigate(NavRoutes.study(it)) },
            )
        }
        composable(
            route = NavRoutes.DECK_DETAIL,
            arguments = listOf(navArgument("deckId") { type = NavType.LongType }),
        ) { entry ->
            val deckId = entry.arguments?.getLong("deckId") ?: return@composable
            val vm: DeckDetailViewModel = viewModel(factory = DeckDetailViewModel.Factory(repository, deckId))
            DeckDetailScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onStudy = { navController.navigate(NavRoutes.study(deckId)) },
            )
        }
        composable(
            route = NavRoutes.STUDY,
            arguments = listOf(navArgument("deckId") { type = NavType.LongType }),
        ) { entry ->
            val deckId = entry.arguments?.getLong("deckId") ?: return@composable
            val vm: StudyViewModel = viewModel(factory = StudyViewModel.Factory(repository, deckId))
            StudyScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
    }
}
