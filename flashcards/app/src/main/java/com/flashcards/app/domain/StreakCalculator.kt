package com.flashcards.app.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

data class WeekDayStreak(
    val label: String,
    val date: String,
    val isToday: Boolean,
    val studied: Boolean,
)

data class StreakInfo(
    val currentStreak: Int,
    val weekDays: List<WeekDayStreak>,
    val studiedDaysThisWeek: Int,
)

object StreakCalculator {
    fun studiedDates(decks: List<Deck>): Set<String> {
        val fromSchedule = decks.flatMap { deck ->
            deck.schedule.filter { it.completed }.map { it.date }
        }
        val fromReviews = decks.mapNotNull { it.sr.lastReview }
        return (fromSchedule + fromReviews).toSet()
    }

    fun compute(decks: List<Deck>): StreakInfo {
        val studied = studiedDates(decks)
        val today = LocalDate.now()
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekDays = (0..6).map { offset ->
            val date = monday.plusDays(offset.toLong())
            val dateStr = DateUtils.toDateString(date)
            WeekDayStreak(
                label = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()).uppercase(),
                date = dateStr,
                isToday = date == today,
                studied = dateStr in studied,
            )
        }
        return StreakInfo(
            currentStreak = currentStreak(studied, today),
            weekDays = weekDays,
            studiedDaysThisWeek = weekDays.count { it.studied },
        )
    }

    private fun currentStreak(studied: Set<String>, today: LocalDate): Int {
        if (studied.isEmpty()) return 0
        var streak = 0
        var cursor = today
        if (DateUtils.toDateString(cursor) !in studied) {
            cursor = cursor.minusDays(1)
        }
        while (DateUtils.toDateString(cursor) in studied) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }
}
