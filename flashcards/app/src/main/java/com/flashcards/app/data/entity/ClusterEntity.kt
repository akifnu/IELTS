package com.flashcards.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clusters")
data class ClusterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String = "📁",
    val sortOrder: Int = 0,
)
