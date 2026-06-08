package com.flashcards.app.domain

data class Cluster(
    val id: Long = 0,
    val name: String,
    val emoji: String = "📁",
    val sortOrder: Int = 0,
)

data class AlgoConfig(
    val enabled: Boolean = false,
    val algorithm: String = "ebbinghaus",
    val preset: String = "normal",
    val intervals: List<Int> = listOf(1, 2, 4, 7, 15, 30, 60, 120),
    val initialStability: Double = 1.0,
    val initialEase: Double = 2.5,
    val minEase: Double = 1.3,
    val maxEase: Double = 3.0,
    val easeBonus: Double = 0.15,
    val easePenalty: Double = 0.25,
    val retentionTarget: Double = 0.9,
    val maxSessionsPerDay: Int = 1,
    val failIntervalDays: Int = 1,
    val curveSteepness: Double = 1.0,
)

data class DeckSr(
    val repetitions: Int = 0,
    val ease: Double = 2.5,
    val stability: Double = 1.0,
    val intervalDays: Int = 0,
    val lastReview: String? = null,
)

data class ScheduleEntry(
    val id: Long,
    val date: String,
    val source: String = "auto",
    val completed: Boolean = false,
)

data class Sm2State(
    val repetitions: Int = 0,
    val easeFactor: Double = 2.5,
    val intervalDays: Int = 0,
)

data class Ownership(
    val userId: String = "guest",
    val email: String? = null,
    val name: String = "Guest",
)

data class Collaborator(
    val email: String,
    val role: String = "viewer",
    val grantId: String? = null,
    val addedAt: String? = null,
)

data class Sharing(
    val collaborators: List<Collaborator> = emptyList(),
)

data class DeckAccess(
    val role: String,
    val grantId: String? = null,
    val ownerEmail: String? = null,
    val ownerName: String? = null,
    val ownerId: String? = null,
    val acceptedAt: String? = null,
    val accessType: String = "granted",
    val lastSyncedAt: String? = null,
)

data class Flashcard(
    val id: Long = 0,
    val deckId: Long,
    val front: String,
    val back: String,
    val color: String? = null,
    val leitnerBox: Int = 1,
    val sm2: Sm2State = Sm2State(),
    val sortOrder: Int = 0,
    val timesStudied: Int = 0,
    val timesCorrect: Int = 0,
)

data class Deck(
    val id: Long = 0,
    val clusterId: Long? = null,
    val name: String,
    val description: String = "",
    val ebbinghaus: Boolean = false,
    val algo: AlgoConfig = AlgoConfig(),
    val sr: DeckSr = DeckSr(),
    val schedule: List<ScheduleEntry> = emptyList(),
    val ownership: Ownership = Ownership(),
    val sharing: Sharing = Sharing(),
    val access: DeckAccess? = null,
    val cards: List<Flashcard> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
)

data class AppSettings(
    val globalMaxSessionsPerDay: Int = 3,
    val userName: String = "",
    val onboarded: Boolean = false,
)

data class InboxItem(
    val id: String,
    val payloadJson: String,
    val fromEmail: String? = null,
    val fromName: String? = null,
    val role: String = "viewer",
    val receivedAt: Long = System.currentTimeMillis(),
)

data class BackupData(
    val shine: Int = 1,
    val version: Int = 2,
    val clusters: List<Cluster> = emptyList(),
    val decks: List<Deck> = emptyList(),
    val globalMaxSessionsPerDay: Int = 3,
    val userName: String = "",
)

data class CardColor(
    val id: String,
    val hex: String?,
    val border: String,
    val label: String,
)

object ShineConstants {
    val CARD_COLORS = listOf(
        CardColor("none", null, "#e5e7eb", "Default"),
        CardColor("yellow", "#fff8e1", "#f9a825", "Yellow"),
        CardColor("pink", "#fce4ec", "#c2185b", "Pink"),
        CardColor("purple", "#f3e5f5", "#7b1fa2", "Purple"),
        CardColor("orange", "#fff3e0", "#ef6c00", "Orange"),
        CardColor("blue-dark", "#1565c0", "#0d47a1", "Blue"),
        CardColor("green-dark", "#2e7d32", "#1b5e20", "Green"),
        CardColor("slate-dark", "#37474f", "#263238", "Slate"),
    )

    val ALGORITHMS = mapOf(
        "ebbinghaus" to "Ebbinghaus",
        "leitner" to "Leitner",
        "sm2" to "SM-2",
    )

    val PRESETS = mapOf(
        "relaxed" to AlgoConfig(
            preset = "relaxed",
            intervals = listOf(2, 4, 8, 16, 32, 64),
            retentionTarget = 0.85,
            failIntervalDays = 2,
        ),
        "normal" to AlgoConfig(
            preset = "normal",
            intervals = listOf(1, 2, 4, 7, 15, 30, 60),
            retentionTarget = 0.9,
            failIntervalDays = 1,
        ),
        "intensive" to AlgoConfig(
            preset = "intensive",
            intervals = listOf(1, 1, 2, 4, 7, 14, 21),
            retentionTarget = 0.95,
            failIntervalDays = 1,
        ),
    )

    val LEITNER_DAYS = listOf(1, 2, 4, 7, 14)
}
