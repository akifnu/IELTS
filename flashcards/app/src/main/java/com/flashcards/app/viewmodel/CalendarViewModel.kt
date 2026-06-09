package com.flashcards.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashcards.app.data.ShineRepository
import com.flashcards.app.domain.AppSettings
import com.flashcards.app.domain.DateUtils
import com.flashcards.app.domain.Deck
import com.flashcards.app.domain.SpacedRepetitionEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class CalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val selectedDate: String = DateUtils.todayStr(),
    val decks: List<Deck> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val scheduleDeckId: Long? = null,
    val overloadDays: List<Pair<String, Int>> = emptyList(),
) {
    fun sessionsOn(date: String): List<Pair<Deck, com.flashcards.app.domain.ScheduleEntry>> {
        val result = mutableListOf<Pair<Deck, com.flashcards.app.domain.ScheduleEntry>>()
        decks.forEach { deck ->
            SpacedRepetitionEngine.sessionsOn(deck, date).forEach { session ->
                result.add(deck to session)
            }
        }
        return result
    }
}

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: ShineRepository,
) : ViewModel() {
    private val monthFlow = kotlinx.coroutines.flow.MutableStateFlow(YearMonth.now())
    private val selectedDateFlow = kotlinx.coroutines.flow.MutableStateFlow(DateUtils.todayStr())

    val uiState: StateFlow<CalendarUiState> = combine(
        monthFlow,
        selectedDateFlow,
        repository.observeDecks(),
        repository.observeSettings(),
    ) { month, selected, decks, settings ->
        val scheduleDeckId = decks.firstOrNull()?.id
        val overload = repository.globalOverloadDays(decks, settings.globalMaxSessionsPerDay)
        CalendarUiState(
            month = month,
            selectedDate = selected,
            decks = decks,
            settings = settings,
            scheduleDeckId = scheduleDeckId,
            overloadDays = overload,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    fun prevMonth() {
        monthFlow.value = monthFlow.value.minusMonths(1)
    }

    fun nextMonth() {
        monthFlow.value = monthFlow.value.plusMonths(1)
    }

    fun selectDate(date: String) {
        selectedDateFlow.value = date
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
}
