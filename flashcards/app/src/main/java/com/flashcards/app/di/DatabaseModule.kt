package com.flashcards.app.di

import android.content.Context
import androidx.room.Room
import com.flashcards.app.data.ShineDatabase
import com.flashcards.app.data.dao.ClusterDao
import com.flashcards.app.data.dao.DeckDao
import com.flashcards.app.data.dao.FlashcardDao
import com.flashcards.app.data.dao.InboxDao
import com.flashcards.app.data.dao.SettingsDao
import com.flashcards.app.data.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ShineDatabase =
        Room.databaseBuilder(context, ShineDatabase::class.java, "shine.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideClusterDao(db: ShineDatabase): ClusterDao = db.clusterDao()
    @Provides fun provideDeckDao(db: ShineDatabase): DeckDao = db.deckDao()
    @Provides fun provideFlashcardDao(db: ShineDatabase): FlashcardDao = db.flashcardDao()
    @Provides fun provideSettingsDao(db: ShineDatabase): SettingsDao = db.settingsDao()
    @Provides fun provideInboxDao(db: ShineDatabase): InboxDao = db.inboxDao()
    @Provides fun provideUserDao(db: ShineDatabase): UserDao = db.userDao()
}
