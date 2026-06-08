package com.flashcards.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.flashcards.app.data.ShineRepository
import com.flashcards.app.domain.AppSettings
import com.flashcards.app.domain.DateUtils
import com.flashcards.app.domain.Deck
import com.flashcards.app.domain.ScheduleEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CalendarUiState(
    val month: LocalDate = LocalDate.now().withDayOfMonth(1),
    val selectedDate: String = DateUtils.todayStr(),
    val decks: List<Deck> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val scheduleDeckId: Long? = null,
) {
    val overloadDays: List<Pair<String, Int>>
        get() {
            val max = settings.globalMaxSessionsPerDay
            val counts = mutableMapOf<String, Int>()
            decks.forEach { d ->
                d.schedule.filter { !it.completed }.forEach { s ->
                    counts[s.date] = (counts[s.date] ?: 0) + 1
                }
            }
            return counts.filter { it.value > max }.map { it.key to it.value }
        }

    fun sessionsOn(date: String): List<Pair<Deck, ScheduleEntry>> {
        val out = mutableListOf<Pair<Deck, ScheduleEntry>>()
        decks.forEach { d ->
            d.schedule.filter { it.date == date && !it.completed }.forEach { s ->
                out.add(d to s)
            }
        }
        return out
    }
}

class CalendarViewModel(private val repository: ShineRepository) : ViewModel() {
    private val monthFlow = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    private val selectedFlow = MutableStateFlow(DateUtils.todayStr())
    private val scheduleDeckFlow = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<CalendarUiState> = combine(
        monthFlow,
        selectedFlow,
        scheduleDeckFlow,
        repository.observeDecks(),
        repository.observeSettings(),
    ) { month, selected, scheduleDeckId, decks, settings ->
        CalendarUiState(month, selected, decks, settings, scheduleDeckId ?: decks.firstOrNull()?.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    fun prevMonth() {
        monthFlow.update { it.minusMonths(1) }
    }

    fun nextMonth() {
        monthFlow.update { it.plusMonths(1) }
    }

    fun selectDate(date: String) {
        selectedFlow.value = date
    }

    fun setScheduleDeck(deckId: Long) {
        scheduleDeckFlow.value = deckId
    }

    fun addSession(date: String, deckId: Long) {
        viewModelScope.launch { repository.addSession(deckId, date) }
    }

    fun removeSession(deckId: Long, sessionId: Long) {
        viewModelScope.launch { repository.removeSession(deckId, sessionId) }
    }

    fun clearDay(date: String) {
        viewModelScope.launch { repository.clearDay(date) }
    }

    fun spreadOverload() {
        viewModelScope.launch { repository.spreadGlobalOverload() }
    }

    fun setMaxSessionsPerDay(max: Int) {
        viewModelScope.launch { repository.setGlobalMaxSessionsPerDay(max) }
    }

    class Factory(private val repository: ShineRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CalendarViewModel(repository) as T
    }
}
