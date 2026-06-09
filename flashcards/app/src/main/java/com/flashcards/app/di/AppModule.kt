package com.flashcards.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.flashcards.app.data.ShineRepository
import com.flashcards.app.data.auth.SessionStore
import com.flashcards.app.data.dao.ClusterDao
import com.flashcards.app.data.dao.DeckDao
import com.flashcards.app.data.dao.FlashcardDao
import com.flashcards.app.data.dao.InboxDao
import com.flashcards.app.data.dao.SettingsDao
import com.flashcards.app.data.dao.UserDao
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.sessionDataStore by preferencesDataStore("shine_session")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideSessionStore(@ApplicationContext context: Context): SessionStore =
        SessionStore(context.sessionDataStore)

    @Provides
    @Singleton
    fun provideShineRepository(
        clusterDao: ClusterDao,
        deckDao: DeckDao,
        flashcardDao: FlashcardDao,
        settingsDao: SettingsDao,
        inboxDao: InboxDao,
        gson: Gson,
    ): ShineRepository = ShineRepository(
        clusterDao,
        deckDao,
        flashcardDao,
        settingsDao,
        inboxDao,
        gson,
    )
}
