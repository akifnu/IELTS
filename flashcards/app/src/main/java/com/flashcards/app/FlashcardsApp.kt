package com.flashcards.app

import android.app.Application
import com.flashcards.app.data.FlashcardsDatabase
import com.flashcards.app.data.FlashcardsRepository

class FlashcardsApp : Application() {
    val repository: FlashcardsRepository by lazy {
        val database = FlashcardsDatabase.getInstance(this)
        FlashcardsRepository(database.deckDao(), database.flashcardDao())
    }
}
