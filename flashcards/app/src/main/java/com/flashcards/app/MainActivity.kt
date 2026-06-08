package com.flashcards.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.flashcards.app.navigation.FlashcardsNavHost
import com.flashcards.app.ui.theme.FlashcardsTheme

/** Native Jetpack Compose shell — no WebView. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlashcardsTheme {
                FlashcardsNavHost()
            }
        }
    }
}
