package com.flashcards.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.flashcards.app.data.entity.FlashcardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards ORDER BY deckId, sortOrder, id")
    fun observeAll(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId ORDER BY sortOrder, id")
    fun observeForDeck(deckId: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId ORDER BY sortOrder, id")
    suspend fun getForDeck(deckId: Long): List<FlashcardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: FlashcardEntity): Long

    @Update
    suspend fun update(card: FlashcardEntity)

    @Delete
    suspend fun delete(card: FlashcardEntity)
}
