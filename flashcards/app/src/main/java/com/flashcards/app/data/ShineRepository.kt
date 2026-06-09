package com.flashcards.app.data

import com.flashcards.app.data.dao.ClusterDao
import com.flashcards.app.data.dao.DeckDao
import com.flashcards.app.data.dao.FlashcardDao
import com.flashcards.app.data.dao.InboxDao
import com.flashcards.app.data.dao.SettingsDao
import com.flashcards.app.data.entity.AppSettingsEntity
import com.flashcards.app.data.entity.ClusterEntity
import com.flashcards.app.data.entity.DeckEntity
import com.flashcards.app.data.entity.FlashcardEntity
import com.flashcards.app.domain.AlgoConfig
import com.flashcards.app.domain.AppSettings
import com.flashcards.app.domain.BackupData
import com.flashcards.app.domain.Cluster
import com.flashcards.app.domain.Collaborator
import com.flashcards.app.domain.DateUtils
import com.flashcards.app.domain.Deck
import com.flashcards.app.domain.DeckAccess
import com.flashcards.app.domain.DeckPermissions
import com.flashcards.app.domain.DeckSr
import com.flashcards.app.domain.Flashcard
import com.flashcards.app.domain.InboxItem
import com.flashcards.app.domain.Ownership
import com.flashcards.app.domain.ShineConstants
import com.flashcards.app.domain.Sharing
import com.flashcards.app.domain.SpacedRepetitionEngine
import com.flashcards.app.domain.ShareCodec
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.util.UUID

class ShineRepository(
    private val clusterDao: ClusterDao,
    private val deckDao: DeckDao,
    private val flashcardDao: FlashcardDao,
    private val settingsDao: SettingsDao,
    private val inboxDao: InboxDao,
    private val gson: Gson,
) {

    fun observeSettings(): Flow<AppSettings> =
        settingsDao.observe().map { e ->
            e?.let { Mappers.settingsFromEntity(it) } ?: AppSettings()
        }

    fun observeClusters(): Flow<List<Cluster>> =
        clusterDao.observeAll().map { list -> list.map { Mappers.clusterFromEntity(it) } }

    fun observeDecks(): Flow<List<Deck>> = combine(
        deckDao.observeAll(),
        flashcardDao.observeAll(),
    ) { deckEntities, cardEntities ->
        val cardsByDeck = cardEntities
            .map { Mappers.cardFromEntity(it) }
            .groupBy { it.deckId }
        deckEntities.map { e ->
            Mappers.deckFromEntity(e, cardsByDeck[e.id].orEmpty())
        }
    }

    fun observeDeck(deckId: Long): Flow<Deck?> =
        combine(
            deckDao.observeById(deckId),
            flashcardDao.observeForDeck(deckId),
        ) { deckEntity, cardEntities ->
            deckEntity?.let { e ->
                Mappers.deckFromEntity(e, cardEntities.map { Mappers.cardFromEntity(it) })
            }
        }

    fun observeInbox(): Flow<List<InboxItem>> =
        inboxDao.observeAll().map { list -> list.map { Mappers.inboxFromEntity(it) } }

    suspend fun getSettings(): AppSettings {
        val e = settingsDao.get()
        return e?.let { Mappers.settingsFromEntity(it) } ?: AppSettings()
    }

    private suspend fun saveSettings(settings: AppSettings) {
        settingsDao.upsert(Mappers.settingsToEntity(settings))
    }

    suspend fun updateUserName(name: String) {
        val s = getSettings()
        saveSettings(s.copy(userName = name.trim()))
    }

    suspend fun setGlobalMaxSessionsPerDay(max: Int) {
        val s = getSettings()
        saveSettings(s.copy(globalMaxSessionsPerDay = max.coerceIn(1, 10)))
    }

    suspend fun markOnboarded() {
        val s = getSettings()
        saveSettings(s.copy(onboarded = true))
    }

    suspend fun createCluster(name: String, emoji: String = "📁"): Long {
        val clusters = clusterDao.observeAll().first()
        return clusterDao.insert(
            Mappers.clusterToEntity(
                Cluster(name = name.trim(), emoji = emoji, sortOrder = clusters.size),
            ),
        )
    }

    suspend fun updateCluster(cluster: Cluster) {
        clusterDao.update(Mappers.clusterToEntity(cluster))
    }

    suspend fun deleteCluster(cluster: Cluster) {
        val decks = deckDao.observeAll().first()
        decks.filter { it.clusterId == cluster.id }.forEach { deck ->
            deckDao.update(deck.copy(clusterId = null))
        }
        clusterDao.delete(Mappers.clusterToEntity(cluster))
    }

    suspend fun createDeck(name: String, description: String, clusterId: Long?): Long {
        val settings = getSettings()
        val ownership = Ownership(
            userId = "guest",
            name = settings.userName.ifBlank { "Guest" },
        )
        val deck = Deck(
            name = name.trim(),
            description = description.trim(),
            clusterId = clusterId,
            ownership = ownership,
            algo = AlgoConfig(),
            sr = DeckSr(ease = AlgoConfig().initialEase, stability = AlgoConfig().initialStability),
        )
        return deckDao.insert(Mappers.deckToEntity(deck))
    }

    suspend fun updateDeck(deck: Deck) {
        deckDao.update(Mappers.deckToEntity(deck))
    }

    suspend fun deleteDeck(deckId: Long) {
        val deck = deckDao.getById(deckId) ?: return
        deckDao.delete(deck)
    }

    suspend fun leaveSharedDeck(deckId: Long) {
        val deck = getDeck(deckId) ?: return
        if (!DeckPermissions.canLeave(deck)) return
        deleteDeck(deckId)
    }

    suspend fun getDeck(deckId: Long): Deck? {
        val e = deckDao.getById(deckId) ?: return null
        val cards = flashcardDao.getForDeck(deckId).map { Mappers.cardFromEntity(it) }
        return Mappers.deckFromEntity(e, cards)
    }

    suspend fun addCard(deckId: Long, front: String, back: String, color: String? = null): Long {
        val cards = flashcardDao.getForDeck(deckId)
        return flashcardDao.insert(
            Mappers.cardToEntity(
                Flashcard(
                    deckId = deckId,
                    front = front.trim(),
                    back = back.trim(),
                    color = color,
                    sortOrder = cards.size,
                ),
            ),
        )
    }

    suspend fun updateCard(card: Flashcard) {
        flashcardDao.update(Mappers.cardToEntity(card))
    }

    suspend fun deleteCard(card: Flashcard) {
        flashcardDao.delete(Mappers.cardToEntity(card))
    }

    suspend fun reorderCards(deckId: Long, orderedCardIds: List<Long>) {
        orderedCardIds.forEachIndexed { index, cardId ->
            val entity = flashcardDao.getForDeck(deckId).find { it.id == cardId } ?: return@forEachIndexed
            flashcardDao.update(entity.copy(sortOrder = index))
        }
    }

    suspend fun seedSampleData() {
        val existing = clusterDao.observeAll().first()
        if (existing.isNotEmpty()) return

        val clusters = listOf(
            ClusterEntity(name = "Languages", emoji = "🌍", sortOrder = 0),
            ClusterEntity(name = "Wellness", emoji = "🧘", sortOrder = 1),
            ClusterEntity(name = "Life & Skills", emoji = "✨", sortOrder = 2),
        )
        val clusterIds = clusters.map { clusterDao.insert(it) }

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
            val deckId = deckDao.insert(
                DeckEntity(
                    clusterId = clusterId,
                    name = name,
                    description = desc,
                    algoJson = gson.toJson(AlgoConfig()),
                ),
            )
            cards.forEachIndexed { i, (front, back) ->
                flashcardDao.insert(
                    FlashcardEntity(deckId = deckId, front = front, back = back, sortOrder = i),
                )
            }
        }
        markOnboarded()
    }

    suspend fun setScheduleMode(deckId: Long, enabled: Boolean) {
        val deck = getDeck(deckId) ?: return
        if (!DeckPermissions.canChangeSettings(deck)) return
        var updated = deck.copy(
            ebbinghaus = enabled,
            algo = deck.algo.copy(enabled = enabled),
        )
        if (enabled) {
            updated = applyPreset(updated, "normal")
            updated = updated.copy(schedule = SpacedRepetitionEngine.seedSmartSchedule(updated))
        }
        updateDeck(updated)
    }

    suspend fun applyPreset(deckId: Long, presetKey: String) {
        val deck = getDeck(deckId) ?: return
        if (!DeckPermissions.canChangeSettings(deck)) return
        updateDeck(applyPreset(deck, presetKey))
    }

    private fun applyPreset(deck: Deck, presetKey: String): Deck {
        val preset = ShineConstants.PRESETS[presetKey] ?: return deck
        return deck.copy(
            algo = deck.algo.copy(
                preset = presetKey,
                intervals = preset.intervals,
                retentionTarget = preset.retentionTarget,
                failIntervalDays = preset.failIntervalDays,
                enabled = deck.ebbinghaus || deck.algo.enabled,
            ),
        )
    }

    suspend fun setAlgorithm(deckId: Long, algorithm: String) {
        val deck = getDeck(deckId) ?: return
        if (!DeckPermissions.canChangeSettings(deck)) return
        updateDeck(deck.copy(algo = deck.algo.copy(algorithm = algorithm)))
    }

    suspend fun setClusterForDeck(deckId: Long, clusterId: Long?) {
        val deck = getDeck(deckId) ?: return
        if (!DeckPermissions.canChangeSettings(deck)) return
        updateDeck(deck.copy(clusterId = clusterId))
    }

    suspend fun finishStudy(deckId: Long, cardResults: List<Pair<Flashcard, Boolean>>) {
        val deck = getDeck(deckId) ?: return
        val today = DateUtils.todayStr()
        var updatedCards = deck.cards.toMutableList()
        cardResults.forEach { (card, recalled) ->
            val idx = updatedCards.indexOfFirst { it.id == card.id }
            if (idx >= 0) {
                val reviewed = SpacedRepetitionEngine.onCardReview(card, deck, recalled)
                val studied = reviewed.copy(
                    timesStudied = reviewed.timesStudied + 1,
                    timesCorrect = reviewed.timesCorrect + if (recalled) 1 else 0,
                )
                updatedCards[idx] = studied
                flashcardDao.update(Mappers.cardToEntity(studied))
            }
        }
        val total = cardResults.size
        val correct = cardResults.count { it.second }
        val score = if (total > 0) correct.toDouble() / total else 0.5

        var schedule = deck.schedule
        if (cardResults.isNotEmpty()) {
            schedule = if (schedule.any { it.date == today }) {
                schedule.map { s ->
                    if (s.date == today) s.copy(completed = true) else s
                }
            } else {
                schedule + com.flashcards.app.domain.ScheduleEntry(
                    id = System.currentTimeMillis(),
                    date = today,
                    source = "study",
                    completed = true,
                )
            }
        }
        var sr = deck.sr
        if (DeckPermissions.isSmartScheduleOn(deck)) {
            val deckWithCards = deck.copy(cards = updatedCards)
            val (nextDate, intervalDays) = SpacedRepetitionEngine.computeNextReview(deckWithCards, score)
            schedule = schedule + SpacedRepetitionEngine.newScheduleEntry(nextDate, "auto")
            sr = deck.sr.copy(lastReview = today, intervalDays = intervalDays)
        } else if (cardResults.isNotEmpty()) {
            sr = deck.sr.copy(lastReview = today)
        }
        updateDeck(deck.copy(cards = updatedCards, schedule = schedule, sr = sr))
    }

    suspend fun addSession(deckId: Long, date: String) {
        val deck = getDeck(deckId) ?: return
        val schedule = deck.schedule + SpacedRepetitionEngine.newScheduleEntry(date, "manual")
        updateDeck(deck.copy(schedule = schedule))
    }

    suspend fun removeSession(deckId: Long, sessionId: Long) {
        val deck = getDeck(deckId) ?: return
        updateDeck(deck.copy(schedule = deck.schedule.filter { it.id != sessionId }))
    }

    suspend fun clearDay(date: String) {
        val decks = deckDao.observeAll().first()
        decks.forEach { e ->
            val deck = getDeck(e.id) ?: return@forEach
            updateDeck(
                deck.copy(schedule = deck.schedule.filter { it.date != date || it.completed }),
            )
        }
    }

    suspend fun spreadGlobalOverload() {
        val settings = getSettings()
        val max = settings.globalMaxSessionsPerDay
        val decks = observeDecks().first()
        globalOverloadDays(decks, max).forEach { (date, _) ->
            val items = allSessionsOn(date)
            items.drop(max).forEach { (deck, session) ->
                for (i in 1..60) {
                    val target = DateUtils.addDaysStr(date, i)
                    if (countSessionsOn(target) < max) {
                        val updated = deck.schedule.map { s ->
                            if (s.id == session.id) s.copy(date = target) else s
                        }
                        updateDeck(deck.copy(schedule = updated))
                        break
                    }
                }
            }
        }
    }

    fun allSessionsOn(date: String, decks: List<Deck>): List<Pair<Deck, com.flashcards.app.domain.ScheduleEntry>> {
        val out = mutableListOf<Pair<Deck, com.flashcards.app.domain.ScheduleEntry>>()
        decks.forEach { d ->
            SpacedRepetitionEngine.sessionsOn(d, date).forEach { s ->
                out.add(d to s)
            }
        }
        return out
    }

    suspend fun allSessionsOn(date: String): List<Pair<Deck, com.flashcards.app.domain.ScheduleEntry>> {
        val decks = observeDecks().first()
        return allSessionsOn(date, decks)
    }

    suspend fun countSessionsOn(date: String): Int =
        allSessionsOn(date).size

    fun globalOverloadDays(decks: List<Deck>, max: Int): List<Pair<String, Int>> {
        val counts = mutableMapOf<String, Int>()
        decks.forEach { d ->
            d.schedule.filter { !it.completed }.forEach { s ->
                counts[s.date] = (counts[s.date] ?: 0) + 1
            }
        }
        return counts.filter { it.value > max }.map { it.key to it.value }
    }

    suspend fun addCollaborator(deckId: Long, email: String, role: String) {
        val deck = getDeck(deckId) ?: return
        if (!DeckPermissions.canShare(deck)) return
        val em = email.trim().lowercase()
        if (em.isBlank()) return
        val grantId = UUID.randomUUID().toString()
        val collabs = deck.sharing.collaborators.toMutableList()
        val existing = collabs.indexOfFirst { it.email == em }
        val collab = Collaborator(email = em, role = role, grantId = grantId, addedAt = DateUtils.todayStr())
        if (existing >= 0) collabs[existing] = collab else collabs.add(collab)
        updateDeck(deck.copy(sharing = Sharing(collabs)))
    }

    suspend fun removeCollaborator(deckId: Long, email: String) {
        val deck = getDeck(deckId) ?: return
        if (!DeckPermissions.canShare(deck)) return
        val em = email.trim().lowercase()
        updateDeck(
            deck.copy(
                sharing = Sharing(
                    deck.sharing.collaborators.filter { it.email != em },
                ),
            ),
        )
    }

    suspend fun updateCollaboratorRole(deckId: Long, email: String, role: String) {
        val deck = getDeck(deckId) ?: return
        if (!DeckPermissions.canShare(deck)) return
        val em = email.trim().lowercase()
        val collabs = deck.sharing.collaborators.map {
            if (it.email == em) it.copy(role = role) else it
        }
        updateDeck(deck.copy(sharing = Sharing(collabs)))
    }

    suspend fun exportBackup(): String {
        val clusters = clusterDao.observeAll().first().map { Mappers.clusterFromEntity(it) }
        val decks = observeDecks().first()
        val settings = getSettings()
        return gson.toJson(
            BackupData(
                clusters = clusters,
                decks = decks,
                globalMaxSessionsPerDay = settings.globalMaxSessionsPerDay,
                userName = settings.userName,
            ),
        )
    }

    suspend fun importBackup(json: String) {
        val data = gson.fromJson(json, BackupData::class.java) ?: return
        clusterDao.observeAll().first().forEach { clusterDao.delete(it) }
        deckDao.observeAll().first().forEach { deckDao.delete(it) }
        val clusterIdMap = mutableMapOf<Long, Long>()
        data.clusters.forEach { c ->
            val newId = clusterDao.insert(Mappers.clusterToEntity(c.copy(id = 0)))
            clusterIdMap[c.id] = newId
        }
        data.decks.forEach { d ->
            val newDeckId = deckDao.insert(
                Mappers.deckToEntity(
                    d.copy(
                        id = 0,
                        clusterId = d.clusterId?.let { clusterIdMap[it] },
                    ),
                ),
            )
            d.cards.forEach { card ->
                flashcardDao.insert(Mappers.cardToEntity(card.copy(id = 0, deckId = newDeckId)))
            }
        }
        saveSettings(
            getSettings().copy(
                globalMaxSessionsPerDay = data.globalMaxSessionsPerDay,
                userName = data.userName,
            ),
        )
    }

    suspend fun buildShareJson(deckId: Long, role: String = "viewer"): String {
        val deck = getDeck(deckId) ?: return "{}"
        val cluster = deck.clusterId?.let { id ->
            clusterDao.getById(id)?.let { Mappers.clusterFromEntity(it) }
        }
        val payload = ShareCodec.buildDeckSharePayload(deck, cluster, role, null, deck.ownership)
        return payload.toString()
    }

    suspend fun acceptSharedDeck(payloadJson: String, clusterId: Long? = null): Long {
        val data = JSONObject(payloadJson)
        val parsed = ShareCodec.parseDeckSharePayload(data) ?: throw IllegalArgumentException("Invalid share")
        val deckObj = parsed.getJSONObject("deck")
        val accessMeta = parsed.optJSONObject("access")
        val clusterHint = parsed.optJSONObject("cluster")
        val role = accessMeta?.optString("role") ?: "viewer"
        val grantId = accessMeta?.optString("grantId")
        val resolvedClusterId = resolveClusterForShare(clusterHint, clusterId)
        val cardsArray = deckObj.getJSONArray("cards")
        val cards = mutableListOf<Flashcard>()
        for (i in 0 until cardsArray.length()) {
            val c = cardsArray.getJSONObject(i)
            cards.add(
                Flashcard(
                    deckId = 0,
                    front = c.optString("front"),
                    back = c.optString("back"),
                    color = c.optString("color").takeIf { it.isNotBlank() },
                    sortOrder = i,
                ),
            )
        }
        val algoJson = deckObj.optJSONObject("algo")
        val algo = if (algoJson != null) {
            gson.fromJson(algoJson.toString(), AlgoConfig::class.java) ?: AlgoConfig()
        } else AlgoConfig()
        val smartOn = deckObj.optBoolean("ebbinghaus", false)
        val access = DeckAccess(
            role = role,
            grantId = grantId,
            ownerEmail = accessMeta?.optString("ownerEmail"),
            ownerName = accessMeta?.optString("ownerName"),
            ownerId = accessMeta?.optString("ownerId"),
            acceptedAt = java.time.Instant.now().toString(),
            accessType = "granted",
            lastSyncedAt = parsed.optString("sharedAt"),
        )
        val deck = Deck(
            name = uniqueDeckName(deckObj.optString("name", "Shared deck")),
            description = deckObj.optString("description", ""),
            clusterId = resolvedClusterId,
            ebbinghaus = smartOn,
            algo = algo.copy(enabled = smartOn),
            access = access,
            cards = emptyList(),
        )
        val deckId = deckDao.insert(Mappers.deckToEntity(deck))
        cards.forEach { flashcardDao.insert(Mappers.cardToEntity(it.copy(deckId = deckId))) }
        markOnboarded()
        return deckId
    }

    suspend fun addToInbox(payloadJson: String, fromEmail: String?, fromName: String?, role: String) {
        inboxDao.insert(
            Mappers.inboxToEntity(
                InboxItem(
                    id = UUID.randomUUID().toString(),
                    payloadJson = payloadJson,
                    fromEmail = fromEmail,
                    fromName = fromName,
                    role = role,
                ),
            ),
        )
    }

    suspend fun dismissInbox(id: String) = inboxDao.deleteById(id)

    private suspend fun resolveClusterForShare(clusterHint: JSONObject?, preferredId: Long?): Long? {
        if (preferredId != null && preferredId > 0) {
            if (clusterDao.getById(preferredId) != null) return preferredId
        }
        val hintName = clusterHint?.optString("name")?.takeIf { it.isNotBlank() }
        if (hintName != null) {
            val match = clusterDao.observeAll().first()
                .map { Mappers.clusterFromEntity(it) }
                .find { it.name.equals(hintName, ignoreCase = true) }
            if (match != null) return match.id
            return clusterDao.insert(
                Mappers.clusterToEntity(
                    Cluster(
                        name = hintName,
                        emoji = clusterHint.optString("emoji", "📁"),
                    ),
                ),
            )
        }
        return clusterDao.observeAll().first().firstOrNull()?.id
    }

    private suspend fun uniqueDeckName(name: String): String {
        val base = name.trim().ifBlank { "Shared deck" }
        val names = deckDao.observeAll().first().map { it.name }.toSet()
        if (!names.contains(base)) return base
        var i = 2
        while (names.contains("$base ($i)")) i++
        return "$base ($i)"
    }
}
