package com.flashcards.app.domain

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

object SpacedRepetitionEngine {
    fun getAlgorithm(deck: Deck): String = deck.algo.algorithm

    fun computeNextReview(deck: Deck, recallScore: Double): Pair<String, Int> {
        val kind = getAlgorithm(deck)
        return when (kind) {
            "leitner" -> computeLeitner(deck)
            "sm2" -> computeSm2(deck)
            else -> computeEbbinghaus(deck, recallScore)
        }
    }

    fun onCardReview(card: Flashcard, deck: Deck, recalled: Boolean): Flashcard {
        return when (getAlgorithm(deck)) {
            "leitner" -> onCardLeitner(card, recalled)
            "sm2" -> onCardSm2(card, recalled)
            else -> card
        }
    }

    private fun computeEbbinghaus(deck: Deck, recallScore: Double): Pair<String, Int> {
        val algo = deck.algo
        val sr = deck.sr
        val passed = recallScore >= 0.6
        val newSr = if (!passed) {
            sr.copy(
                repetitions = 0,
                stability = max(0.5, sr.stability * 0.5),
                ease = max(algo.minEase, sr.ease - algo.easePenalty),
                intervalDays = algo.failIntervalDays,
                lastReview = DateUtils.todayStr(),
            )
        } else {
            val rep = sr.repetitions
            val interval = if (rep < algo.intervals.size) {
                algo.intervals[rep]
            } else {
                intervalFromStability(sr.stability, algo)
            }
            sr.copy(
                repetitions = rep + 1,
                stability = sr.stability * (1 + sr.ease * recallScore),
                ease = min(algo.maxEase, sr.ease + algo.easeBonus * recallScore),
                intervalDays = interval,
                lastReview = DateUtils.todayStr(),
            )
        }
        val nextDate = DateUtils.addDaysStr(DateUtils.todayStr(), newSr.intervalDays)
        return nextDate to newSr.intervalDays
    }

    private fun intervalFromStability(stability: Double, algo: AlgoConfig): Int {
        val target = algo.retentionTarget.coerceIn(0.01, 0.99)
        val t = -stability * ln(target)
        return max(1, (t * algo.curveSteepness).roundToInt())
    }

    private fun onCardLeitner(card: Flashcard, recalled: Boolean): Flashcard {
        val box = if (recalled) min(5, card.leitnerBox + 1) else 1
        return card.copy(leitnerBox = box)
    }

    private fun onCardSm2(card: Flashcard, recalled: Boolean): Flashcard {
        val q = if (recalled) 4 else 1
        val sm = card.sm2
        val updated = if (q < 3) {
            sm.copy(repetitions = 0, intervalDays = 1)
        } else {
            val interval = when {
                sm.repetitions == 0 -> 1
                sm.repetitions == 1 -> 6
                else -> (sm.intervalDays * sm.easeFactor).roundToInt()
            }
            val ease = max(
                1.3,
                sm.easeFactor + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)),
            )
            sm.copy(
                repetitions = sm.repetitions + 1,
                intervalDays = interval,
                easeFactor = ease,
            )
        }
        return card.copy(sm2 = updated)
    }

    private fun computeLeitner(deck: Deck): Pair<String, Int> {
        val boxes = deck.cards.map { it.leitnerBox }
        val avg = if (boxes.isEmpty()) 1.0 else boxes.average()
        val days = leitnerDaysForBox(avg.roundToInt())
        return DateUtils.addDaysStr(DateUtils.todayStr(), days) to days
    }

    private fun computeSm2(deck: Deck): Pair<String, Int> {
        val days = max(1, deck.cards.maxOfOrNull { it.sm2.intervalDays } ?: 1)
        return DateUtils.addDaysStr(DateUtils.todayStr(), days) to days
    }

    fun leitnerDaysForBox(box: Int): Int {
        val idx = (box - 1).coerceIn(0, ShineConstants.LEITNER_DAYS.lastIndex)
        return ShineConstants.LEITNER_DAYS[idx]
    }

    fun sessionsOn(deck: Deck, date: String): List<ScheduleEntry> =
        deck.schedule.filter { it.date == date && !it.completed }

    fun isDue(deck: Deck): Boolean = sessionsOn(deck, DateUtils.todayStr()).isNotEmpty()

    fun nextScheduled(deck: Deck): String? =
        deck.schedule.filter { !it.completed }.map { it.date }.minOrNull()

    fun newScheduleEntry(date: String, source: String = "auto"): ScheduleEntry =
        ScheduleEntry(
            id = System.currentTimeMillis() + (Math.random() * 1000).toLong(),
            date = date,
            source = source,
        )

    fun seedSmartSchedule(deck: Deck): List<ScheduleEntry> {
        if (!DeckPermissions.isSmartScheduleOn(deck)) return deck.schedule
        if (nextScheduled(deck) != null) return deck.schedule
        return deck.schedule + newScheduleEntry(DateUtils.todayStr(), "auto")
    }
}
