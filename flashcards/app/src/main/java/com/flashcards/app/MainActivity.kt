package com.flashcards.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.flashcards.app.navigation.NavRoutes
import com.flashcards.app.ui.screens.DeckDetailScreen
import com.flashcards.app.ui.screens.DeckListScreen
import com.flashcards.app.ui.screens.StudyScreen
import com.flashcards.app.ui.theme.FlashcardsTheme
import com.flashcards.app.viewmodel.DeckDetailViewModel
import com.flashcards.app.viewmodel.DeckListViewModel
import com.flashcards.app.viewmodel.StudyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as FlashcardsApp).repository

        setContent {
            FlashcardsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = NavRoutes.DECK_LIST
                    ) {
                        composable(NavRoutes.DECK_LIST) {
                            val viewModel: DeckListViewModel = viewModel(
                                factory = DeckListViewModel.Factory(repository)
                            )
                            DeckListScreen(
                                viewModel = viewModel,
                                onDeckClick = { deckId ->
                                    navController.navigate(NavRoutes.deckDetail(deckId))
                                }
                            )
                        }

                        composable(
                            route = NavRoutes.DECK_DETAIL,
                            arguments = listOf(
                                navArgument("deckId") { type = NavType.LongType }
                            )
                        ) { backStackEntry ->
                            val deckId = backStackEntry.arguments?.getLong("deckId") ?: return@composable
                            val viewModel: DeckDetailViewModel = viewModel(
                                factory = DeckDetailViewModel.Factory(repository, deckId)
                            )
                            DeckDetailScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onStudy = { navController.navigate(NavRoutes.study(deckId)) }
                            )
                        }

                        composable(
                            route = NavRoutes.STUDY,
                            arguments = listOf(
                                navArgument("deckId") { type = NavType.LongType }
                            )
                        ) { backStackEntry ->
                            val deckId = backStackEntry.arguments?.getLong("deckId") ?: return@composable
                            val viewModel: StudyViewModel = viewModel(
                                factory = StudyViewModel.Factory(repository, deckId)
                            )
                            StudyScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
