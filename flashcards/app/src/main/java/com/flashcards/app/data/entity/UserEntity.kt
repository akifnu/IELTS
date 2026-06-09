package com.flashcards.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val name: String,
    val provider: String,
    val passwordHash: String? = null,
    val salt: String? = null,
    val avatar: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
