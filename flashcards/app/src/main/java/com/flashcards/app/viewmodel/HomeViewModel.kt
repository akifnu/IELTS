package com.flashcards.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashcards.app.data.ShineRepository
import com.flashcards.app.domain.Cluster
import com.flashcards.app.domain.DateUtils
import com.flashcards.app.domain.Deck
import com.flashcards.app.domain.DeckPermissions
import com.flashcards.app.domain.SpacedRepetitionEngine
import com.flashcards.app.domain.StreakCalculator
import com.flashcards.app.domain.StreakInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TodayProgress(
    val dueDeckCount: Int = 0,
    val completedDeckCount: Int = 0,
    val dueCardCount: Int = 0,
    val progressFraction: Float = 0f,
    val allCaughtUp: Boolean = true,
)

data class HomeUiState(
    val clusters: List<Cluster> = emptyList(),
    val decks: List<Deck> = emptyList(),
    val dueDecks: List<Deck> = emptyList(),
    val completedTodayDecks: List<Deck> = emptyList(),
    val todayProgress: TodayProgress = TodayProgress(),
    val ownedDecks: List<Deck> = emptyList(),
    val sharedDecks: List<Deck> = emptyList(),
    val streak: StreakInfo = StreakInfo(0, emptyList(), 0),
)

private fun Deck.completedToday(): Boolean {
    val today = DateUtils.todayStr()
    val sessions = schedule.filter { it.date == today }
    return sessions.isNotEmpty() && sessions.all { it.completed }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ShineRepository,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeClusters(),
        repository.observeDecks(),
    ) { clusters, decks ->
        val dueDecks = decks.filter { SpacedRepetitionEngine.isDue(it) }
        val completedToday = decks.filter { it.completedToday() }
        val dueCount = dueDecks.size
        val completedCount = completedToday.size
        val totalToday = dueCount + completedCount
        HomeUiState(
            clusters = clusters,
            decks = decks,
            dueDecks = dueDecks,
            completedTodayDecks = completedToday,
            todayProgress = TodayProgress(
                dueDeckCount = dueCount,
                completedDeckCount = completedCount,
                dueCardCount = dueDecks.sumOf { it.cards.size },
                progressFraction = if (totalToday == 0) 1f else completedCount.toFloat() / totalToday,
                allCaughtUp = dueCount == 0,
            ),
            ownedDecks = DeckPermissions.ownedDecks(decks),
            sharedDecks = DeckPermissions.sharedWithMe(decks),
            streak = StreakCalculator.compute(decks),
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
}
