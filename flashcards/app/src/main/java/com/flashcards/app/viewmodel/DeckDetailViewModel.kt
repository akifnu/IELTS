package com.flashcards.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.flashcards.app.data.Deck
import com.flashcards.app.data.Flashcard
import com.flashcards.app.data.FlashcardsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DeckDetailViewModel(
    private val repository: FlashcardsRepository,
    private val deckId: Long
) : ViewModel() {

    val deck: StateFlow<Deck?> = repository.getDeck(deckId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val cards: StateFlow<List<Flashcard>> = repository.getCards(deckId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addCard(front: String, back: String, onDone: () -> Unit) {
        if (front.isBlank() || back.isBlank()) return
        viewModelScope.launch {
            repository.createCard(deckId, front, back)
            onDone()
        }
    }

    fun updateCard(card: Flashcard, front: String, back: String, onDone: () -> Unit) {
        if (front.isBlank() || back.isBlank()) return
        viewModelScope.launch {
            repository.updateCard(card.copy(front = front.trim(), back = back.trim()))
            onDone()
        }
    }

    fun deleteCard(card: Flashcard) {
        viewModelScope.launch {
            repository.deleteCard(card)
        }
    }

    class Factory(
        private val repository: FlashcardsRepository,
        private val deckId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeckDetailViewModel(repository, deckId) as T
        }
    }
}
