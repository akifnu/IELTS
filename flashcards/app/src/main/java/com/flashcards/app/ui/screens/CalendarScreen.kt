package com.flashcards.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flashcards.app.domain.DateUtils
import com.flashcards.app.domain.Deck
import com.flashcards.app.ui.theme.ShinePrimary
import com.flashcards.app.ui.theme.ShinePrimaryLight
import com.flashcards.app.viewmodel.CalendarViewModel
import java.time.format.TextStyle
import java.util.Locale

private val CalendarBlue = Color(0xFF3B82F6)
private val CalendarBlueSoft = Color(0xFFDBEAFE)
private val OverloadBg = Color(0xFFFEF2F2)
private val OverloadBorder = Color(0xFFFCA5A5)
private val WarnAmber = Color(0xFF92400E)

@OptIn(ExperimentalMaterial3Api::class)
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
    ) {
        if (state.overloadDays.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "⚠ ${state.overloadDays.size} busy day${if (state.overloadDays.size > 1) "s" else ""}",
                            fontWeight = FontWeight.Bold,
                            color = WarnAmber,
                        )
                        Text(
                            "More than ${state.settings.globalMaxSessionsPerDay} deck sessions on one day.",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarnAmber.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                        )
                        Button(
                            onClick = { viewModel.spreadOverload() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        ) { Text("Spread sessions out") }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = { viewModel.prevMonth() },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = ShinePrimary)
                        }
                        Text(monthLabel, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        IconButton(
                            onClick = { viewModel.nextMonth() },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = ShinePrimary)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth()) {
                        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { label ->
                            Text(
                                label,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    paddedCells.chunked(7).forEach { week ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            week.forEach { day ->
                                if (day == null) {
                                    Spacer(Modifier.weight(1f).height(56.dp))
                                } else {
                                    CalendarDayCell(
                                        day = day,
                                        year = year,
                                        month = month,
                                        state = state,
                                        onSelect = { viewModel.selectDate(it) },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        LegendDot(color = CalendarBlue, label = "Deck scheduled")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(OverloadBg)
                                    .border(2.dp, OverloadBorder, RoundedCornerShape(3.dp)),
                            )
                            Text(
                                "Too many",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 6.dp),
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        DateUtils.formatDate(state.selectedDate),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                    Spacer(Modifier.height(12.dp))
                    val sessions = state.sessionsOn(state.selectedDate)
                    if (sessions.isEmpty()) {
                        Text(
                            "Nothing scheduled — add a deck below",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        sessions.forEach { (deck, session) ->
                            SessionRow(
                                deck = deck,
                                source = session.source,
                                canStudy = state.selectedDate <= DateUtils.todayStr(),
                                onStudy = { onStudyDeck(deck.id) },
                                onRemove = { viewModel.removeSession(deck.id, session.id) },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    if (state.decks.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        DeckPicker(
                            decks = state.decks,
                            selectedId = state.scheduleDeckId,
                            onSelect = viewModel::setScheduleDeck,
                        )
                        Button(
                            onClick = {
                                state.scheduleDeckId?.let { viewModel.addSession(state.selectedDate, it) }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) { Text("Add study session") }
                    }
                    if (sessions.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { viewModel.clearDay(state.selectedDate) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) { Text("Clear this day") }
                    }
                }
            }
        }

    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    year: Int,
    month: Int,
    state: com.flashcards.app.viewmodel.CalendarUiState,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ds = String.format("%04d-%02d-%02d", year, month, day)
    val sessions = state.sessionsOn(ds)
    val overloaded = sessions.size > state.settings.globalMaxSessionsPerDay
    val isToday = ds == DateUtils.todayStr()
    val selected = ds == state.selectedDate

    val bg = when {
        overloaded -> OverloadBg
        selected -> MaterialTheme.colorScheme.surface
        isToday -> ShinePrimaryLight
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    }
    val borderColor = when {
        overloaded -> OverloadBorder
        selected -> ShinePrimary
        isToday -> ShinePrimary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    }
    val borderWidth = if (selected) 2.dp else 1.dp

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .background(bg)
            .clickable { onSelect(ds) }
            .padding(horizontal = 4.dp, vertical = 4.dp),
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                "$day",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 2.dp),
            )
            Spacer(Modifier.height(2.dp))
            sessions.take(2).forEach { (deck, _) ->
                Text(
                    deck.name.take(8),
                    fontSize = 9.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(CalendarBlue)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                )
                Spacer(Modifier.height(2.dp))
            }
            if (sessions.size > 2) {
                Text(
                    "+${sessions.size - 2}",
                    fontSize = 9.sp,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CalendarBlue)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
        }
    }
}


@Composable
private fun SessionRow(
    deck: Deck,
    source: String,
    canStudy: Boolean,
    onStudy: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(deck.name, fontWeight = FontWeight.SemiBold)
            Text(source, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row {
            if (canStudy) {
                TextButton(onClick = onStudy) {
                    Text("Study", color = CalendarBlue, fontWeight = FontWeight.SemiBold)
                }
            }
            TextButton(onClick = onRemove) {
                Text("Remove", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeckPicker(
    decks: List<Deck>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = decks.find { it.id == selectedId }?.name ?: "Select deck"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Which deck?") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            decks.forEach { deck ->
                DropdownMenuItem(
                    text = { Text(deck.name) },
                    onClick = {
                        onSelect(deck.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color),
        )
        Text(
            label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}
