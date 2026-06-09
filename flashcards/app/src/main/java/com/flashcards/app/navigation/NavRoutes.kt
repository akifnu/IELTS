package com.flashcards.app.navigation

object NavRoutes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val MAIN = "main"
    const val DECK_DETAIL = "deck/{deckId}"
    const val STUDY = "study/{deckId}"

    fun deckDetail(deckId: Long) = "deck/$deckId"
    fun study(deckId: Long) = "study/$deckId"
}
