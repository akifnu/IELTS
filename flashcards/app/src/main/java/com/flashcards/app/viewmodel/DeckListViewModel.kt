package com.flashcards.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.flashcards.app.data.Deck
import com.flashcards.app.data.DeckWithCardCount
import com.flashcards.app.data.FlashcardsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DeckListViewModel(
    private val repository: FlashcardsRepository
) : ViewModel() {

    val decks: StateFlow<List<DeckWithCardCount>> = repository.getDecksWithCardCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createDeck(name: String, description: String, onDone: () -> Unit) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createDeck(name, description)
            onDone()
        }
    }

    fun deleteDeck(deck: Deck) {
        viewModelScope.launch {
            repository.deleteDeck(deck)
        }
    }

    class Factory(private val repository: FlashcardsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeckListViewModel(repository) as T
        }
    }
}
