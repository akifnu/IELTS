package com.flashcards.app

import android.app.Application
import com.flashcards.app.data.ShineDatabase
import com.flashcards.app.data.ShineRepository

class FlashcardsApp : Application() {
    val repository: ShineRepository by lazy {
        val db = ShineDatabase.getInstance(this)
        ShineRepository(
            db.clusterDao(),
            db.deckDao(),
            db.flashcardDao(),
            db.settingsDao(),
            db.inboxDao(),
        )
    }
}
