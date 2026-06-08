package com.flashcards.app.domain

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject
import java.nio.charset.StandardCharsets

object ShareCodec {
    private val gson = Gson()

    fun encodeSharePayload(obj: Any): String {
        val json = gson.toJson(obj)
        val encoded = Base64.encodeToString(
            json.toByteArray(StandardCharsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING,
        )
        return encoded
    }

    fun decodeSharePayload(encoded: String): JSONObject? {
        return try {
            var b64 = encoded.trim().replace('-', '+').replace('_', '/')
            while (b64.length % 4 != 0) b64 += "="
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            JSONObject(String(bytes, StandardCharsets.UTF_8))
        } catch (_: Exception) {
            null
        }
    }

    fun buildDeckSharePayload(
        deck: Deck,
        cluster: Cluster?,
        role: String = "viewer",
        grantId: String? = null,
        owner: Ownership,
    ): JSONObject {
        val algo = deck.algo
        val smartOn = DeckPermissions.isSmartScheduleOn(deck)
        val root = JSONObject()
        root.put("shine", 1)
        root.put("kind", "deck")
        root.put("v", 2)
        root.put("sharedAt", DateUtils.todayStr())
        root.put(
            "access",
            JSONObject().apply {
                put("role", role)
                put("grantId", grantId)
                put("accessType", "granted")
                put("ownerEmail", owner.email)
                put("ownerId", owner.userId)
                put("ownerName", owner.name)
            },
        )
        if (cluster != null) {
            root.put(
                "cluster",
                JSONObject().apply {
                    put("name", cluster.name)
                    put("emoji", cluster.emoji)
                },
            )
        }
        val cards = org.json.JSONArray()
        deck.cards.forEach { c ->
            cards.put(
                JSONObject().apply {
                    put("front", c.front)
                    put("back", c.back)
                    put("color", c.color)
                },
            )
        }
        root.put(
            "deck",
            JSONObject().apply {
                put("name", deck.name)
                put("description", deck.description)
                put("ebbinghaus", smartOn)
                put(
                    "algo",
                    JSONObject().apply {
                        put("algorithm", algo.algorithm)
                        put("preset", algo.preset)
                        put("enabled", smartOn)
                        put("intervals", org.json.JSONArray(algo.intervals))
                        put("retentionTarget", algo.retentionTarget)
                        put("maxSessionsPerDay", algo.maxSessionsPerDay)
                    },
                )
                put("cards", cards)
            },
        )
        return root
    }

    fun parseDeckSharePayload(data: JSONObject): JSONObject? {
        if (data.optString("kind") == "deck" && data.optInt("shine") == 1 && data.has("deck")) {
            return data
        }
        if (data.has("name") && data.has("cards") && !data.has("decks")) {
            return JSONObject().apply {
                put("shine", 1)
                put("kind", "deck")
                put("v", 1)
                put("deck", data)
            }
        }
        return null
    }
}
