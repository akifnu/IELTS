package com.flashcards.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.flashcards.app.data.ShineRepository
import com.flashcards.app.domain.AppSettings
import com.flashcards.app.domain.Deck
import com.flashcards.app.domain.InboxItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AccountUiState(
    val settings: AppSettings = AppSettings(),
    val deckCount: Int = 0,
    val clusterCount: Int = 0,
    val inbox: List<InboxItem> = emptyList(),
    val decks: List<Deck> = emptyList(),
)

class AccountViewModel(private val repository: ShineRepository) : ViewModel() {
    val uiState: StateFlow<AccountUiState> = combine(
        repository.observeSettings(),
        repository.observeDecks(),
        repository.observeClusters(),
        repository.observeInbox(),
    ) { settings, decks, clusters, inbox ->
        AccountUiState(
            settings = settings,
            deckCount = decks.size,
            clusterCount = clusters.size,
            inbox = inbox,
            decks = decks,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountUiState())

    fun updateUserName(name: String) {
        viewModelScope.launch { repository.updateUserName(name) }
    }

    suspend fun exportBackup(): String = repository.exportBackup()

    fun importBackup(json: String, onDone: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.importBackup(json)
                onDone()
            } catch (_: Exception) {
                onError()
            }
        }
    }

    fun acceptInbox(item: InboxItem, clusterId: Long?, onDone: (Long) -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                val deckId = repository.acceptSharedDeck(item.payloadJson, clusterId)
                repository.dismissInbox(item.id)
                onDone(deckId)
            } catch (_: Exception) {
                onError()
            }
        }
    }

    fun dismissInbox(id: String) {
        viewModelScope.launch { repository.dismissInbox(id) }
    }

    fun importShare(json: String, clusterId: Long?, onDone: (Long) -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                val deckId = repository.acceptSharedDeck(json, clusterId)
                onDone(deckId)
            } catch (_: Exception) {
                onError()
            }
        }
    }

    class Factory(private val repository: ShineRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AccountViewModel(repository) as T
    }
}
