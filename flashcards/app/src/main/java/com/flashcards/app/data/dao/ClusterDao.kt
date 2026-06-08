package com.flashcards.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.flashcards.app.data.entity.ClusterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClusterDao {
    @Query("SELECT * FROM clusters ORDER BY sortOrder, id")
    fun observeAll(): Flow<List<ClusterEntity>>

    @Query("SELECT * FROM clusters WHERE id = :id")
    suspend fun getById(id: Long): ClusterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cluster: ClusterEntity): Long

    @Update
    suspend fun update(cluster: ClusterEntity)

    @Delete
    suspend fun delete(cluster: ClusterEntity)
}
