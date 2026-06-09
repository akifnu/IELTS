package com.flashcards.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.flashcards.app.ui.screens.DeckDetailScreen
import com.flashcards.app.ui.screens.MainTabsScreen
import com.flashcards.app.ui.screens.OnboardingScreen
import com.flashcards.app.ui.screens.SplashScreen
import com.flashcards.app.ui.screens.StudyScreen

@Composable
fun ShineNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH,
        modifier = modifier,
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen(
                onSignedIn = {
                    navController.navigate(NavRoutes.MAIN) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNeedOnboarding = {
                    navController.navigate(NavRoutes.ONBOARDING) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                },
            )
        }
        composable(NavRoutes.ONBOARDING) {
            OnboardingScreen(
                onDone = {
                    navController.navigate(NavRoutes.MAIN) {
                        popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }
        composable(NavRoutes.MAIN) {
            MainTabsScreen(
                onOpenDeck = { navController.navigate(NavRoutes.deckDetail(it)) },
                onStudyDeck = { navController.navigate(NavRoutes.study(it)) },
            )
        }
        composable(
            route = NavRoutes.DECK_DETAIL,
            arguments = listOf(navArgument("deckId") { type = NavType.LongType }),
        ) {
            val vm = hiltViewModel<com.flashcards.app.viewmodel.DeckDetailViewModel>()
            DeckDetailScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onStudy = {
                    vm.uiState.value.deck?.id?.let { id ->
                        navController.navigate(NavRoutes.study(id))
                    }
                },
            )
        }
        composable(
            route = NavRoutes.STUDY,
            arguments = listOf(navArgument("deckId") { type = NavType.LongType }),
        ) {
            StudyScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() },
            )
        }
    }
}
