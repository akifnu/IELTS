package com.flashcards.app

import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/** Native bridge: file share + platform marker for the bundled web app. */
class ShineBridge(private val activity: MainActivity) {

    @android.webkit.JavascriptInterface
    fun shareFile(filename: String, json: String, title: String) {
        activity.runOnUiThread {
            try {
                val safeName = filename.replace(Regex("[^a-zA-Z0-9._-]"), "-").ifBlank { "shine-deck.json" }
                val file = File(activity.cacheDir, safeName)
                file.writeText(json)
                val uri = FileProvider.getUriForFile(
                    activity,
                    "${activity.packageName}.fileprovider",
                    file,
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, title)
                    putExtra(Intent.EXTRA_TEXT, "A Shine flashcard deck — open in Shine to import")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                activity.startActivity(Intent.createChooser(intent, "Share deck"))
            } catch (_: Exception) {
            }
        }
    }

    @android.webkit.JavascriptInterface
    fun getPlatform(): String = "android"
}
