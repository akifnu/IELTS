package com.flashcards.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inbox")
data class InboxEntity(
    @PrimaryKey val id: String,
    val payloadJson: String,
    val fromEmail: String? = null,
    val fromName: String? = null,
    val role: String = "viewer",
    val receivedAt: Long = System.currentTimeMillis(),
)
