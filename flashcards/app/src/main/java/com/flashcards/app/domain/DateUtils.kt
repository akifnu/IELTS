package com.flashcards.app.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun todayStr(): String = LocalDate.now().format(fmt)

    fun parseDate(s: String): LocalDate = LocalDate.parse(s, fmt)

    fun addDaysStr(s: String, n: Int): String = parseDate(s).plusDays(n.toLong()).format(fmt)

    fun formatDate(s: String): String {
        val d = parseDate(s)
        return d.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()))
    }

    fun daysInMonth(year: Int, month: Int): Int =
        LocalDate.of(year, month, 1).lengthOfMonth()

    fun firstWeekdayOfMonth(year: Int, month: Int): Int {
        val d = LocalDate.of(year, month, 1)
        return d.dayOfWeek.value % 7 // Sun=0
    }
}
