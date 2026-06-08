package com.flashcards.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {
    @Query(
        """
        SELECT d.*, COUNT(f.id) AS cardCount
        FROM decks d
        LEFT JOIN flashcards f ON f.deckId = d.id
        GROUP BY d.id
        ORDER BY d.createdAt DESC
        """
    )
    fun getDecksWithCardCount(): Flow<List<DeckWithCardCountRow>>

    @Query("SELECT * FROM decks WHERE id = :deckId")
    fun getDeckById(deckId: Long): Flow<Deck?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(deck: Deck): Long

    @Update
    suspend fun update(deck: Deck)

    @Delete
    suspend fun delete(deck: Deck)
}

data class DeckWithCardCountRow(
    val id: Long,
    val name: String,
    val description: String,
    val createdAt: Long,
    val cardCount: Int
) {
    fun toDeckWithCardCount(): DeckWithCardCount = DeckWithCardCount(
        deck = Deck(id = id, name = name, description = description, createdAt = createdAt),
        cardCount = cardCount
    )
}
