package com.flashcards.app.navigation

object NavRoutes {
    const val DECK_LIST = "deck_list"
    const val DECK_DETAIL = "deck_detail/{deckId}"
    const val STUDY = "study/{deckId}"

    fun deckDetail(deckId: Long) = "deck_detail/$deckId"
    fun study(deckId: Long) = "study/$deckId"
}
