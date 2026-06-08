package com.flashcards.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("deckId")],
)
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deckId: Long,
    val front: String,
    val back: String,
    val color: String? = null,
    val leitnerBox: Int = 1,
    val sm2Json: String = "{}",
    val sortOrder: Int = 0,
    val timesStudied: Int = 0,
    val timesCorrect: Int = 0,
)
