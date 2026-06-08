package com.flashcards.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flashcards.app.domain.DateUtils
import com.flashcards.app.viewmodel.CalendarViewModel
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onStudyDeck: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    val year = state.month.year
    val month = state.month.monthValue
    val days = DateUtils.daysInMonth(year, month)
    val firstDay = DateUtils.firstWeekdayOfMonth(year, month)
    val monthLabel = state.month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " $year"
    val dayCells = buildList {
        repeat(firstDay) { add(null) }
        for (d in 1..days) add(d)
    }
    val paddedCells = dayCells.toMutableList()
    while (paddedCells.size % 7 != 0) paddedCells.add(null)

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
    ) {
        if (state.overloadDays.isNotEmpty()) {
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("⚠ ${state.overloadDays.size} busy day(s)", fontWeight = FontWeight.Bold)
                        Text("More than ${state.settings.globalMaxSessionsPerDay} sessions on one day", style = MaterialTheme.typography.bodySmall)
                        Button(onClick = { viewModel.spreadOverload() }, modifier = Modifier.padding(top = 8.dp)) { Text("Spread sessions out") }
                    }
                }
            }
        }
        item {
            Card {
                Column(Modifier.padding(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { viewModel.prevMonth() }) { Text("‹") }
                        Text(monthLabel, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { viewModel.nextMonth() }) { Text("›") }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                            Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    paddedCells.chunked(7).forEach { week ->
                        Row(Modifier.fillMaxWidth()) {
                            week.forEach { day ->
                                if (day == null) {
                                    Box(Modifier.weight(1f).aspectRatio(1f))
                                } else {
                                    val ds = String.format("%04d-%02d-%02d", year, month, day)
                                    val count = state.sessionsOn(ds).size
                                    val overloaded = count > state.settings.globalMaxSessionsPerDay
                                    val isToday = ds == DateUtils.todayStr()
                                    val selected = ds == state.selectedDate
                                    Box(
                                        Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .background(
                                                when {
                                                    selected -> MaterialTheme.colorScheme.primaryContainer
                                                    isToday -> MaterialTheme.colorScheme.secondaryContainer
                                                    overloaded -> Color(0xFFFFF7ED)
                                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                                },
                                            )
                                            .border(if (selected) 2.dp else 0.dp, MaterialTheme.colorScheme.primary)
                                            .clickable { viewModel.selectDate(ds) },
                                        contentAlignment = Alignment.TopCenter,
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("$day", style = MaterialTheme.typography.labelMedium)
                                            if (count > 0) {
                                                Text("$count", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Card {
                Column(Modifier.padding(16.dp)) {
                    Text(DateUtils.formatDate(state.selectedDate), fontWeight = FontWeight.Bold)
                    val sessions = state.sessionsOn(state.selectedDate)
                    if (sessions.isEmpty()) Text("Nothing scheduled", style = MaterialTheme.typography.bodySmall)
                    sessions.forEach { (deck, session) ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(deck.name, modifier = Modifier.weight(1f))
                            Row {
                                if (state.selectedDate <= DateUtils.todayStr()) {
                                    TextButton(onClick = { onStudyDeck(deck.id) }) { Text("Study") }
                                }
                                TextButton(onClick = { viewModel.removeSession(deck.id, session.id) }) { Text("Remove") }
                            }
                        }
                    }
                    val deckId = state.scheduleDeckId
                    if (deckId != null && state.decks.isNotEmpty()) {
                        Button(
                            onClick = { viewModel.addSession(state.selectedDate, deckId) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        ) { Text("Add session for ${state.decks.find { it.id == deckId }?.name ?: "deck"}") }
                    }
                    if (sessions.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { viewModel.clearDay(state.selectedDate) },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        ) { Text("Clear this day") }
                    }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Busy-day limit: ${state.settings.globalMaxSessionsPerDay}")
                TextButton(onClick = { viewModel.setMaxSessionsPerDay(state.settings.globalMaxSessionsPerDay - 1) }) { Text("−") }
                TextButton(onClick = { viewModel.setMaxSessionsPerDay(state.settings.globalMaxSessionsPerDay + 1) }) { Text("+") }
            }
        }
    }
}
