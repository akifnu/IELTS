package com.flashcards.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FlashcardsRepository(
    private val deckDao: DeckDao,
    private val flashcardDao: FlashcardDao
) {
    fun getDecksWithCardCount(): Flow<List<DeckWithCardCount>> =
        deckDao.getDecksWithCardCount().map { rows ->
            rows.map { it.toDeckWithCardCount() }
        }

    fun getDeck(deckId: Long): Flow<Deck?> = deckDao.getDeckById(deckId)

    fun getCards(deckId: Long): Flow<List<Flashcard>> = flashcardDao.getCardsForDeck(deckId)

    suspend fun createDeck(name: String, description: String): Long =
        deckDao.insert(Deck(name = name.trim(), description = description.trim()))

    suspend fun updateDeck(deck: Deck) = deckDao.update(deck)

    suspend fun deleteDeck(deck: Deck) = deckDao.delete(deck)

    suspend fun createCard(deckId: Long, front: String, back: String): Long =
        flashcardDao.insert(
            Flashcard(deckId = deckId, front = front.trim(), back = back.trim())
        )

    suspend fun updateCard(card: Flashcard) = flashcardDao.update(card)

    suspend fun deleteCard(card: Flashcard) = flashcardDao.delete(card)

    suspend fun recordStudyResult(cardId: Long, correct: Boolean) =
        flashcardDao.recordStudyResult(cardId, if (correct) 1 else 0)
}
