package com.flashcards.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.flashcards.app.data.dao.ClusterDao
import com.flashcards.app.data.dao.DeckDao
import com.flashcards.app.data.dao.FlashcardDao
import com.flashcards.app.data.dao.InboxDao
import com.flashcards.app.data.dao.SettingsDao
import com.flashcards.app.data.entity.AppSettingsEntity
import com.flashcards.app.data.entity.ClusterEntity
import com.flashcards.app.data.entity.DeckEntity
import com.flashcards.app.data.entity.FlashcardEntity
import com.flashcards.app.data.entity.InboxEntity
import com.flashcards.app.domain.AlgoConfig
import com.flashcards.app.domain.Cluster
import com.flashcards.app.domain.Deck
import com.flashcards.app.domain.Flashcard
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ClusterEntity::class,
        DeckEntity::class,
        FlashcardEntity::class,
        AppSettingsEntity::class,
        InboxEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class ShineDatabase : RoomDatabase() {
    abstract fun clusterDao(): ClusterDao
    abstract fun deckDao(): DeckDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun settingsDao(): SettingsDao
    abstract fun inboxDao(): InboxDao

    companion object {
        @Volatile
        private var INSTANCE: ShineDatabase? = null

        fun getInstance(context: Context): ShineDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): ShineDatabase {
            return Room.databaseBuilder(context, ShineDatabase::class.java, "shine.db")
                .fallbackToDestructiveMigration()
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

        private suspend fun seedSampleData(database: ShineDatabase) {
            val gson = Gson()
            database.settingsDao().upsert(AppSettingsEntity(onboarded = true))

            val clusters = listOf(
                ClusterEntity(name = "Languages", emoji = "🌍", sortOrder = 0),
                ClusterEntity(name = "Wellness", emoji = "🧘", sortOrder = 1),
                ClusterEntity(name = "Life & Skills", emoji = "✨", sortOrder = 2),
            )
            val clusterIds = clusters.map { database.clusterDao().insert(it) }

            val sampleDecks = listOf(
                Triple(clusterIds[0], "Spanish Phrases", "Everyday conversation") to listOf(
                    "Hello" to "Hola",
                    "Thank you" to "Gracias",
                    "How are you?" to "¿Cómo estás?",
                ),
                Triple(clusterIds[0], "French Basics", "Travel essentials") to listOf(
                    "Good morning" to "Bonjour",
                    "Please" to "S'il vous plaît",
                    "Where is…?" to "Où est…?",
                ),
                Triple(clusterIds[1], "Mindfulness", "Calm daily practices") to listOf(
                    "Box breathing" to "Inhale 4s · hold 4s · exhale 4s · hold 4s",
                    "Body scan" to "Notice tension from toes to head",
                    "Gratitude pause" to "Name 3 things you appreciate today",
                ),
                Triple(clusterIds[2], "Coffee & Recipes", "Kitchen know-how") to listOf(
                    "Espresso ratio" to "1:2 coffee to liquid · ~25–30s shot",
                    "Vinaigrette base" to "3 parts oil · 1 part acid",
                ),
            )

            sampleDecks.forEach { (meta, cards) ->
                val (clusterId, name, desc) = meta
                val deckId = database.deckDao().insert(
                    DeckEntity(
                        clusterId = clusterId,
                        name = name,
                        description = desc,
                        algoJson = gson.toJson(AlgoConfig()),
                    ),
                )
                cards.forEachIndexed { i, (front, back) ->
                    database.flashcardDao().insert(
                        FlashcardEntity(deckId = deckId, front = front, back = back, sortOrder = i),
                    )
                }
            }
        }
    }
}
