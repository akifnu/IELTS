package com.flashcards.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clusterId: Long? = null,
    val name: String,
    val description: String = "",
    val ebbinghaus: Boolean = false,
    val algoJson: String = "{}",
    val srJson: String = "{}",
    val scheduleJson: String = "[]",
    val ownershipJson: String = "{}",
    val sharingJson: String = "{\"collaborators\":[]}",
    val accessJson: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
