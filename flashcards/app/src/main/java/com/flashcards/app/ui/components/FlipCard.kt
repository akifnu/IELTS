package com.flashcards.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flashcards.app.ui.util.cardBackgroundColor
import com.flashcards.app.ui.util.cardBorderColor
import com.flashcards.app.ui.util.cardForegroundColor

@Composable
fun FlipCard(
    frontText: String,
    backText: String,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    colorId: String? = null,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "cardFlip",
    )
    val bg = cardBackgroundColor(colorId)
    val fg = cardForegroundColor(colorId)

    Card(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .border(2.dp, cardBorderColor(colorId), RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = MutableInteractionSource(),
                indication = null,
                onClick = onFlip,
            ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            val displayText = if (rotation <= 90f) frontText else backText
            val textRotation = if (rotation > 90f) 180f else 0f

            Text(
                text = displayText,
                style = MaterialTheme.typography.headlineSmall,
                color = fg,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { rotationY = textRotation },
            )
        }
    }
}
