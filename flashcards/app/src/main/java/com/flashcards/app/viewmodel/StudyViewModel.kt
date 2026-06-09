package com.flashcards.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashcards.app.data.ShineRepository
import com.flashcards.app.domain.Deck
import com.flashcards.app.domain.Flashcard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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

@HiltViewModel
class StudyViewModel @Inject constructor(
    private val repository: ShineRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val deckId: Long = checkNotNull(savedStateHandle["deckId"])
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
            finishSession(newResults)
        } else {
            _uiState.update {
                it.copy(results = newResults, currentIndex = nextIndex, isFlipped = false)
            }
        }
    }

    fun endEarly() {
        val results = _uiState.value.results
        if (results.isNotEmpty()) finishSession(results)
    }

    private fun finishSession(results: List<Pair<Flashcard, Boolean>>) {
        viewModelScope.launch {
            repository.finishStudy(deckId, results)
        }
        _uiState.update {
            it.copy(results = results, isFlipped = false, isComplete = true)
        }
    }

    fun restart(shuffle: Boolean = true) {
        _uiState.update { state ->
            val cards = if (shuffle) state.deck?.cards?.shuffled().orEmpty() else state.deck?.cards.orEmpty()
            state.copy(
                cards = cards,
                currentIndex = 0,
                isFlipped = false,
                results = emptyList(),
                isComplete = false,
            )
        }
    }

    fun shuffleRemaining() {
        _uiState.update { state ->
            val remaining = state.cards.drop(state.currentIndex).shuffled()
            val kept = state.cards.take(state.currentIndex)
            state.copy(cards = kept + remaining)
        }
    }
}
