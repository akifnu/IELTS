package com.flashcards.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.flashcards.app.data.Flashcard
import com.flashcards.app.data.FlashcardsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudyUiState(
    val cards: List<Flashcard> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val isComplete: Boolean = false,
    val isLoading: Boolean = true
) {
    val currentCard: Flashcard? get() = cards.getOrNull(currentIndex)
    val progress: Float
        get() = if (cards.isEmpty()) 0f else (currentIndex + if (isComplete) 1 else 0).toFloat() / cards.size
    val progressLabel: String
        get() = if (cards.isEmpty()) "0 / 0" else "${minOf(currentIndex + 1, cards.size)} / ${cards.size}"
}

class StudyViewModel(
    private val repository: FlashcardsRepository,
    private val deckId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudyUiState())
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getCards(deckId).collect { cards ->
                _uiState.update {
                    it.copy(
                        cards = cards.shuffled(),
                        isLoading = false,
                        isComplete = cards.isEmpty()
                    )
                }
            }
        }
    }

    fun flipCard() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun markCorrect() {
        recordAnswer(correct = true)
    }

    fun markIncorrect() {
        recordAnswer(correct = false)
    }

    private fun recordAnswer(correct: Boolean) {
        val state = _uiState.value
        val card = state.currentCard ?: return

        viewModelScope.launch {
            repository.recordStudyResult(card.id, correct)
        }

        val nextIndex = state.currentIndex + 1
        if (nextIndex >= state.cards.size) {
            _uiState.update {
                it.copy(
                    isFlipped = false,
                    correctCount = it.correctCount + if (correct) 1 else 0,
                    incorrectCount = it.incorrectCount + if (correct) 0 else 1,
                    isComplete = true
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    currentIndex = nextIndex,
                    isFlipped = false,
                    correctCount = it.correctCount + if (correct) 1 else 0,
                    incorrectCount = it.incorrectCount + if (correct) 0 else 1
                )
            }
        }
    }

    fun restart() {
        _uiState.update { state ->
            state.copy(
                cards = state.cards.shuffled(),
                currentIndex = 0,
                isFlipped = false,
                correctCount = 0,
                incorrectCount = 0,
                isComplete = false
            )
        }
    }

    class Factory(
        private val repository: FlashcardsRepository,
        private val deckId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StudyViewModel(repository, deckId) as T
        }
    }
}
