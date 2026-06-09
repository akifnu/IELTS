package com.flashcards.app.ui.util

import androidx.compose.ui.graphics.Color
import com.flashcards.app.domain.ShineConstants

fun cardBackgroundColor(colorId: String?): Color {
    val def = ShineConstants.CARD_COLORS.find { it.id == (colorId ?: "none") }
        ?: ShineConstants.CARD_COLORS.first()
    return def.hex?.let { parseHex(it) } ?: Color(0xFFF9FAFB)
}

fun cardForegroundColor(colorId: String?): Color {
    val def = ShineConstants.CARD_COLORS.find { it.id == (colorId ?: "none") }
        ?: ShineConstants.CARD_COLORS.first()
    return if (def.hex == null) Color(0xFF111827) else Color.White
}

fun cardBorderColor(colorId: String?): Color {
    val def = ShineConstants.CARD_COLORS.find { it.id == (colorId ?: "none") }
        ?: ShineConstants.CARD_COLORS.first()
    return parseHex(def.border)
}

private fun parseHex(hex: String): Color {
    val clean = hex.removePrefix("#")
    return Color(("FF$clean").toLong(16))
}
