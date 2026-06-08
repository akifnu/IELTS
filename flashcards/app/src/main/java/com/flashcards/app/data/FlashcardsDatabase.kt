package com.flashcards.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Deck::class, Flashcard::class],
    version = 1,
    exportSchema = false
)
abstract class FlashcardsDatabase : RoomDatabase() {
    abstract fun deckDao(): DeckDao
    abstract fun flashcardDao(): FlashcardDao

    companion object {
        @Volatile
        private var INSTANCE: FlashcardsDatabase? = null

        fun getInstance(context: Context): FlashcardsDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): FlashcardsDatabase {
            return Room.databaseBuilder(
                context,
                FlashcardsDatabase::class.java,
                "flashcards.db"
            )
                .addCallback(SeedCallback())
                .build()
        }
    }

    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedSampleData(database)
                }
            }
        }

        private suspend fun seedSampleData(database: FlashcardsDatabase) {
            val deckDao = database.deckDao()
            val cardDao = database.flashcardDao()

            val spanishId = deckDao.insert(
                Deck(name = "Spanish Basics", description = "Essential Spanish vocabulary")
            )
            val kotlinId = deckDao.insert(
                Deck(name = "Kotlin", description = "Programming concepts in Kotlin")
            )

            listOf(
                Flashcard(deckId = spanishId, front = "Hello", back = "Hola"),
                Flashcard(deckId = spanishId, front = "Thank you", back = "Gracias"),
                Flashcard(deckId = spanishId, front = "Good morning", back = "Buenos días"),
                Flashcard(deckId = spanishId, front = "Goodbye", back = "Adiós"),
                Flashcard(deckId = kotlinId, front = "val vs var", back = "val is immutable, var is mutable"),
                Flashcard(deckId = kotlinId, front = "data class", back = "Auto-generates equals, hashCode, copy, and toString"),
                Flashcard(deckId = kotlinId, front = "suspend fun", back = "A function that can be paused and resumed (coroutine)"),
                Flashcard(deckId = kotlinId, front = "companion object", back = "Static-like members scoped to a class")
            ).forEach { cardDao.insert(it) }
        }
    }
}
