package com.flashcards.app.domain

object DeckPermissions {
    fun role(deck: Deck): String = deck.access?.role ?: "owner"

    fun isShared(deck: Deck): Boolean = deck.access != null

    fun isOwner(deck: Deck): Boolean = role(deck) == "owner"

    fun canEdit(deck: Deck): Boolean = role(deck) in listOf("owner", "editor")

    fun canShare(deck: Deck): Boolean = isOwner(deck)

    fun canDelete(deck: Deck): Boolean = isOwner(deck)

    fun canLeave(deck: Deck): Boolean = isShared(deck)

    fun canChangeSettings(deck: Deck): Boolean = canEdit(deck)

    fun isSmartScheduleOn(deck: Deck): Boolean = deck.ebbinghaus || deck.algo.enabled

    fun ownedDecks(decks: List<Deck>): List<Deck> = decks.filter { !isShared(it) }

    fun sharedWithMe(decks: List<Deck>): List<Deck> = decks.filter { isShared(it) }
}
