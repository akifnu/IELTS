package com.flashcards.app.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.flashcards.app.domain.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionStore(private val dataStore: DataStore<Preferences>) {

    val session: Flow<UserSession> = dataStore.data.map { prefs ->
        val provider = prefs[KEY_PROVIDER] ?: "guest"
        UserSession(
            id = prefs[KEY_ID] ?: "guest",
            email = prefs[KEY_EMAIL],
            name = prefs[KEY_NAME] ?: "Guest",
            provider = provider,
            avatar = prefs[KEY_AVATAR],
        )
    }

    suspend fun save(session: UserSession) {
        dataStore.edit { prefs ->
            prefs[KEY_ID] = session.id
            prefs[KEY_NAME] = session.name
            prefs[KEY_PROVIDER] = session.provider
            if (session.email != null) prefs[KEY_EMAIL] = session.email else prefs.remove(KEY_EMAIL)
            if (session.avatar != null) prefs[KEY_AVATAR] = session.avatar else prefs.remove(KEY_AVATAR)
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    companion object {
        private val KEY_ID = stringPreferencesKey("id")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_NAME = stringPreferencesKey("name")
        private val KEY_PROVIDER = stringPreferencesKey("provider")
        private val KEY_AVATAR = stringPreferencesKey("avatar")
    }
}
