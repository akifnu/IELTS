package com.flashcards.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.flashcards.app.data.dao.ClusterDao
import com.flashcards.app.data.dao.DeckDao
import com.flashcards.app.data.dao.FlashcardDao
import com.flashcards.app.data.dao.InboxDao
import com.flashcards.app.data.dao.SettingsDao
import com.flashcards.app.data.dao.UserDao
import com.flashcards.app.data.entity.AppSettingsEntity
import com.flashcards.app.data.entity.ClusterEntity
import com.flashcards.app.data.entity.DeckEntity
import com.flashcards.app.data.entity.FlashcardEntity
import com.flashcards.app.data.entity.InboxEntity
import com.flashcards.app.data.entity.UserEntity

@Database(
    entities = [
        ClusterEntity::class,
        DeckEntity::class,
        FlashcardEntity::class,
        AppSettingsEntity::class,
        InboxEntity::class,
        UserEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class ShineDatabase : RoomDatabase() {
    abstract fun clusterDao(): ClusterDao
    abstract fun deckDao(): DeckDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun settingsDao(): SettingsDao
    abstract fun inboxDao(): InboxDao
    abstract fun userDao(): UserDao
}
