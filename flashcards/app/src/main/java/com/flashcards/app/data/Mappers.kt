package com.flashcards.app.data

import com.flashcards.app.data.entity.AppSettingsEntity
import com.flashcards.app.data.entity.ClusterEntity
import com.flashcards.app.data.entity.DeckEntity
import com.flashcards.app.data.entity.FlashcardEntity
import com.flashcards.app.data.entity.InboxEntity
import com.flashcards.app.domain.AlgoConfig
import com.flashcards.app.domain.AppSettings
import com.flashcards.app.domain.Cluster
import com.flashcards.app.domain.Collaborator
import com.flashcards.app.domain.Deck
import com.flashcards.app.domain.DeckAccess
import com.flashcards.app.domain.DeckSr
import com.flashcards.app.domain.Flashcard
import com.flashcards.app.domain.InboxItem
import com.flashcards.app.domain.Ownership
import com.flashcards.app.domain.ScheduleEntry
import com.flashcards.app.domain.Sharing
import com.flashcards.app.domain.Sm2State
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Mappers {
    private val gson = Gson()

    fun clusterFromEntity(e: ClusterEntity) = Cluster(e.id, e.name, e.emoji, e.sortOrder)

    fun clusterToEntity(c: Cluster) = ClusterEntity(c.id, c.name, c.emoji, c.sortOrder)

    fun cardFromEntity(e: FlashcardEntity) = Flashcard(
        id = e.id,
        deckId = e.deckId,
        front = e.front,
        back = e.back,
        color = e.color,
        leitnerBox = e.leitnerBox,
        sm2 = gson.fromJson(e.sm2Json, Sm2State::class.java) ?: Sm2State(),
        sortOrder = e.sortOrder,
        timesStudied = e.timesStudied,
        timesCorrect = e.timesCorrect,
    )

    fun cardToEntity(c: Flashcard) = FlashcardEntity(
        id = c.id,
        deckId = c.deckId,
        front = c.front,
        back = c.back,
        color = c.color,
        leitnerBox = c.leitnerBox,
        sm2Json = gson.toJson(c.sm2),
        sortOrder = c.sortOrder,
        timesStudied = c.timesStudied,
        timesCorrect = c.timesCorrect,
    )

    fun deckFromEntity(e: DeckEntity, cards: List<Flashcard> = emptyList()) = Deck(
        id = e.id,
        clusterId = e.clusterId,
        name = e.name,
        description = e.description,
        ebbinghaus = e.ebbinghaus,
        algo = gson.fromJson(e.algoJson, AlgoConfig::class.java) ?: AlgoConfig(),
        sr = gson.fromJson(e.srJson, DeckSr::class.java) ?: DeckSr(),
        schedule = gson.fromJson<List<ScheduleEntry>>(
            e.scheduleJson,
            object : TypeToken<List<ScheduleEntry>>() {}.type,
        ) ?: emptyList(),
        ownership = gson.fromJson(e.ownershipJson, Ownership::class.java) ?: Ownership(),
        sharing = gson.fromJson(e.sharingJson, Sharing::class.java) ?: Sharing(),
        access = e.accessJson?.let { gson.fromJson(it, DeckAccess::class.java) },
        cards = cards,
        createdAt = e.createdAt,
    )

    fun deckToEntity(d: Deck) = DeckEntity(
        id = d.id,
        clusterId = d.clusterId,
        name = d.name,
        description = d.description,
        ebbinghaus = d.ebbinghaus,
        algoJson = gson.toJson(d.algo),
        srJson = gson.toJson(d.sr),
        scheduleJson = gson.toJson(d.schedule),
        ownershipJson = gson.toJson(d.ownership),
        sharingJson = gson.toJson(d.sharing),
        accessJson = d.access?.let { gson.toJson(it) },
        createdAt = d.createdAt,
    )

    fun settingsFromEntity(e: AppSettingsEntity) = AppSettings(
        globalMaxSessionsPerDay = e.globalMaxSessionsPerDay,
        userName = e.userName,
        onboarded = e.onboarded,
    )

    fun settingsToEntity(s: AppSettings) = AppSettingsEntity(
        globalMaxSessionsPerDay = s.globalMaxSessionsPerDay,
        userName = s.userName,
        onboarded = s.onboarded,
    )

    fun inboxFromEntity(e: InboxEntity) = InboxItem(
        id = e.id,
        payloadJson = e.payloadJson,
        fromEmail = e.fromEmail,
        fromName = e.fromName,
        role = e.role,
        receivedAt = e.receivedAt,
    )

    fun inboxToEntity(i: InboxItem) = InboxEntity(
        id = i.id,
        payloadJson = i.payloadJson,
        fromEmail = i.fromEmail,
        fromName = i.fromName,
        role = i.role,
        receivedAt = i.receivedAt,
    )
}
