package com.flashcards.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.flashcards.app.data.entity.DeckEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {
    @Query("SELECT * FROM decks ORDER BY id DESC")
    fun observeAll(): Flow<List<DeckEntity>>

    @Query("SELECT * FROM decks WHERE id = :id")
    fun observeById(id: Long): Flow<DeckEntity?>

    @Query("SELECT * FROM decks WHERE id = :id")
    suspend fun getById(id: Long): DeckEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(deck: DeckEntity): Long

    @Update
    suspend fun update(deck: DeckEntity)

    @Delete
    suspend fun delete(deck: DeckEntity)
}
