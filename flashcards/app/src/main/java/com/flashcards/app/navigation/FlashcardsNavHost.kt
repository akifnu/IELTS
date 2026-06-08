package com.flashcards.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.flashcards.app.FlashcardsApp
import com.flashcards.app.ui.screens.DeckDetailScreen
import com.flashcards.app.ui.screens.DeckListScreen
import com.flashcards.app.ui.screens.StudyScreen
import com.flashcards.app.viewmodel.DeckDetailViewModel
import com.flashcards.app.viewmodel.DeckListViewModel
import com.flashcards.app.viewmodel.StudyViewModel

@Composable
fun FlashcardsNavHost() {
    val navController = rememberNavController()
    val repository = (LocalContext.current.applicationContext as FlashcardsApp).repository

    NavHost(
        navController = navController,
        startDestination = NavRoutes.DECK_LIST,
    ) {
        composable(NavRoutes.DECK_LIST) {
            val viewModel: DeckListViewModel = viewModel(
                factory = DeckListViewModel.Factory(repository),
            )
            DeckListScreen(
                viewModel = viewModel,
                onDeckClick = { deckId ->
                    navController.navigate(NavRoutes.deckDetail(deckId))
                },
            )
        }

        composable(
            route = NavRoutes.DECK_DETAIL,
            arguments = listOf(navArgument("deckId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: return@composable
            val viewModel: DeckDetailViewModel = viewModel(
                factory = DeckDetailViewModel.Factory(repository, deckId),
            )
            DeckDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onStudy = { navController.navigate(NavRoutes.study(deckId)) },
            )
        }

        composable(
            route = NavRoutes.STUDY,
            arguments = listOf(navArgument("deckId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: return@composable
            val viewModel: StudyViewModel = viewModel(
                factory = StudyViewModel.Factory(repository, deckId),
            )
            StudyScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
