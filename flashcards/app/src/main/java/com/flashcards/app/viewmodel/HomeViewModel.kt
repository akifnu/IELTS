package com.flashcards.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.flashcards.app.data.ShineRepository
import com.flashcards.app.domain.Cluster
import com.flashcards.app.domain.Deck
import com.flashcards.app.domain.DeckPermissions
import com.flashcards.app.domain.SpacedRepetitionEngine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val clusters: List<Cluster> = emptyList(),
    val decks: List<Deck> = emptyList(),
    val dueDecks: List<Deck> = emptyList(),
    val ownedDecks: List<Deck> = emptyList(),
    val sharedDecks: List<Deck> = emptyList(),
)

class HomeViewModel(private val repository: ShineRepository) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeClusters(),
        repository.observeDecks(),
    ) { clusters, decks ->
        HomeUiState(
            clusters = clusters,
            decks = decks,
            dueDecks = decks.filter { SpacedRepetitionEngine.isDue(it) },
            ownedDecks = DeckPermissions.ownedDecks(decks),
            sharedDecks = DeckPermissions.sharedWithMe(decks),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun createCluster(name: String, emoji: String, onDone: () -> Unit) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createCluster(name, emoji)
            onDone()
        }
    }

    fun updateCluster(cluster: Cluster, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.updateCluster(cluster)
            onDone()
        }
    }

    fun deleteCluster(cluster: Cluster) {
        viewModelScope.launch { repository.deleteCluster(cluster) }
    }

    fun createDeck(name: String, description: String, clusterId: Long?, onDone: () -> Unit) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createDeck(name, description, clusterId)
            onDone()
        }
    }

    fun deleteDeck(deckId: Long) {
        viewModelScope.launch { repository.deleteDeck(deckId) }
    }

    fun leaveDeck(deckId: Long) {
        viewModelScope.launch { repository.leaveSharedDeck(deckId) }
    }

    class Factory(private val repository: ShineRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(repository) as T
    }
}
