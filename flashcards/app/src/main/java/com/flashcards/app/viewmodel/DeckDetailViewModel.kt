package com.flashcards.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.flashcards.app.data.ShineRepository
import com.flashcards.app.domain.Cluster
import com.flashcards.app.domain.Deck
import com.flashcards.app.domain.DeckPermissions
import com.flashcards.app.domain.Flashcard
import com.flashcards.app.domain.SpacedRepetitionEngine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DeckDetailUiState(
    val deck: Deck? = null,
    val clusters: List<Cluster> = emptyList(),
    val canEdit: Boolean = false,
    val canShare: Boolean = false,
    val smartScheduleOn: Boolean = false,
    val nextScheduled: String? = null,
    val isDue: Boolean = false,
)

class DeckDetailViewModel(
    private val repository: ShineRepository,
    private val deckId: Long,
) : ViewModel() {
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

    fun updateCollaboratorRole(email: String, role: String) {
        viewModelScope.launch { repository.updateCollaboratorRole(deckId, email, role) }
    }

    suspend fun buildShareJson(role: String = "viewer") = repository.buildShareJson(deckId, role)

    fun leaveDeck(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.leaveSharedDeck(deckId)
            onDone()
        }
    }

    class Factory(
        private val repository: ShineRepository,
        private val deckId: Long,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DeckDetailViewModel(repository, deckId) as T
    }
}
