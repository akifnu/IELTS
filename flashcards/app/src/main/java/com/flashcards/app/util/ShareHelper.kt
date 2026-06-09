package com.flashcards.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object ShareHelper {
    fun shareJson(context: Context, filename: String, json: String, title: String) {
        val safeName = filename.replace(Regex("[^a-zA-Z0-9._-]"), "-").ifBlank { "shine-deck.json" }
        val file = File(context.cacheDir, safeName)
        file.writeText(json)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "A Shine flashcard deck — open in Shine to import")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share deck"))
    }
}
