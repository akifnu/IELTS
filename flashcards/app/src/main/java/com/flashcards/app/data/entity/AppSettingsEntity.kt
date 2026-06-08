package com.flashcards.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val globalMaxSessionsPerDay: Int = 3,
    val userName: String = "",
    val onboarded: Boolean = false,
)
