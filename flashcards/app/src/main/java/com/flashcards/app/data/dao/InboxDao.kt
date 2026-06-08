package com.flashcards.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flashcards.app.data.entity.InboxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InboxDao {
    @Query("SELECT * FROM inbox ORDER BY receivedAt DESC")
    fun observeAll(): Flow<List<InboxEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: InboxEntity)

    @Delete
    suspend fun delete(item: InboxEntity)

    @Query("DELETE FROM inbox WHERE id = :id")
    suspend fun deleteById(id: String)
}
