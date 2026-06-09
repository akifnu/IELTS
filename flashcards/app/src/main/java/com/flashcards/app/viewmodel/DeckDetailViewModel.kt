package com.flashcards.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashcards.app.data.ShineRepository
import com.flashcards.app.domain.Cluster
import com.flashcards.app.domain.Deck
import com.flashcards.app.domain.DeckPermissions
import com.flashcards.app.domain.Flashcard
import com.flashcards.app.domain.SpacedRepetitionEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeckDetailUiState(
    val deck: Deck? = null,
    val clusters: List<Cluster> = emptyList(),
    val canEdit: Boolean = false,
    val canShare: Boolean = false,
    val smartScheduleOn: Boolean = false,
    val nextScheduled: String? = null,
    val isDue: Boolean = false,
)

@HiltViewModel
class DeckDetailViewModel @Inject constructor(
    private val repository: ShineRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val deckId: Long = checkNotNull(savedStateHandle["deckId"])

    val uiState: StateFlow<DeckDetailUiState> = combine(
        repository.observeDeck(deckId),
        repository.observeClusters(),
    ) { deck, clusters ->
        DeckDetailUiState(
            deck = deck,
            clusters = clusters,
            canEdit = deck?.let { DeckPermissions.canEdit(it) } == true,
            canShare = deck?.let { DeckPermissions.canShare(it) } == true,
            smartScheduleOn = deck?.let { DeckPermissions.isSmartScheduleOn(it) } == true,
            nextScheduled = deck?.let { SpacedRepetitionEngine.nextScheduled(it) },
            isDue = deck?.let { SpacedRepetitionEngine.isDue(it) } == true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DeckDetailUiState())

    fun addCard(front: String, back: String, color: String?, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.addCard(deckId, front, back, color)
            onDone()
        }
    }

    fun updateCard(card: Flashcard, front: String, back: String, color: String?, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.updateCard(card.copy(front = front.trim(), back = back.trim(), color = color))
            onDone()
        }
    }

    fun deleteCard(card: Flashcard) {
        viewModelScope.launch { repository.deleteCard(card) }
    }

    fun reorderCards(orderedIds: List<Long>) {
        viewModelScope.launch { repository.reorderCards(deckId, orderedIds) }
    }

    fun updateDeck(name: String, description: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val deck = uiState.value.deck ?: return@launch
            repository.updateDeck(deck.copy(name = name.trim(), description = description.trim()))
            onDone()
        }
    }

    fun setScheduleMode(enabled: Boolean) {
        viewModelScope.launch { repository.setScheduleMode(deckId, enabled) }
    }

    fun setAlgorithm(algorithm: String) {
        viewModelScope.launch { repository.setAlgorithm(deckId, algorithm) }
    }

    fun applyPreset(preset: String) {
        viewModelScope.launch { repository.applyPreset(deckId, preset) }
    }

    fun setCluster(clusterId: Long?) {
        viewModelScope.launch { repository.setClusterForDeck(deckId, clusterId) }
    }

    fun addCollaborator(email: String, role: String, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.addCollaborator(deckId, email, role)
            onDone()
        }
    }

    fun removeCollaborator(email: String) {
        viewModelScope.launch { repository.removeCollaborator(deckId, email) }
    }

    suspend fun buildShareJson(role: String = "viewer") = repository.buildShareJson(deckId, role)

    fun leaveDeck(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.leaveSharedDeck(deckId)
            onDone()
        }
    }
}
