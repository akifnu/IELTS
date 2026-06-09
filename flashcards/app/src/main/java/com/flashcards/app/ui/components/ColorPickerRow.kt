package com.flashcards.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.flashcards.app.domain.ShineConstants
import com.flashcards.app.ui.util.cardBackgroundColor
import com.flashcards.app.ui.util.cardBorderColor

@Composable
fun ColorPickerRow(
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(ShineConstants.CARD_COLORS) { color ->
            val isSelected = (selected ?: "none") == color.id
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(cardBackgroundColor(color.id))
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) cardBorderColor(color.id) else Color(0xFFE5E7EB),
                        shape = CircleShape,
                    )
                    .clickable { onSelect(if (color.id == "none") null else color.id) },
            )
        }
    }
}
