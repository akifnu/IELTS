package com.flashcards.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.flashcards.app.data.ShineRepository
import com.flashcards.app.domain.Deck
import com.flashcards.app.domain.Flashcard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudyUiState(
    val deck: Deck? = null,
    val cards: List<Flashcard> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val results: List<Pair<Flashcard, Boolean>> = emptyList(),
    val isComplete: Boolean = false,
    val isLoading: Boolean = true,
) {
    val currentCard: Flashcard? get() = cards.getOrNull(currentIndex)
    val progress: Float
        get() = if (cards.isEmpty()) 0f else (currentIndex + if (isComplete) 1 else 0).toFloat() / cards.size
    val progressLabel: String
        get() = if (cards.isEmpty()) "0 / 0" else "${minOf(currentIndex + 1, cards.size)} / ${cards.size}"
    val correctCount: Int get() = results.count { it.second }
    val incorrectCount: Int get() = results.count { !it.second }
}

class StudyViewModel(
    private val repository: ShineRepository,
    private val deckId: Long,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StudyUiState())
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeDeck(deckId).collect { deck ->
                val cards = deck?.cards?.shuffled().orEmpty()
                _uiState.update {
                    if (it.cards.isEmpty() && !it.isComplete) {
                        it.copy(deck = deck, cards = cards, isLoading = false, isComplete = cards.isEmpty())
                    } else {
                        it.copy(deck = deck, isLoading = false)
                    }
                }
            }
        }
    }

    fun flipCard() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun markCorrect() = recordAnswer(true)

    fun markIncorrect() = recordAnswer(false)

    private fun recordAnswer(correct: Boolean) {
        val state = _uiState.value
        val card = state.currentCard ?: return
        val newResults = state.results + (card to correct)
        val nextIndex = state.currentIndex + 1
        if (nextIndex >= state.cards.size) {
            viewModelScope.launch {
                repository.finishStudy(deckId, newResults)
            }
            _uiState.update {
                it.copy(results = newResults, isFlipped = false, isComplete = true)
            }
        } else {
            _uiState.update {
                it.copy(results = newResults, currentIndex = nextIndex, isFlipped = false)
            }
        }
    }

    fun restart() {
        _uiState.update { state ->
            state.copy(
                cards = state.deck?.cards?.shuffled().orEmpty(),
                currentIndex = 0,
                isFlipped = false,
                results = emptyList(),
                isComplete = false,
            )
        }
    }

    class Factory(
        private val repository: ShineRepository,
        private val deckId: Long,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StudyViewModel(repository, deckId) as T
    }
}
