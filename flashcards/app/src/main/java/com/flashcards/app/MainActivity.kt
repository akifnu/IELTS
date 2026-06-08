package com.flashcards.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.flashcards.app.navigation.ShineNavHost
import com.flashcards.app.ui.theme.FlashcardsTheme

/** Native Jetpack Compose app — Android handles layout/insets on every device. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlashcardsTheme {
                ShineNavHost(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
