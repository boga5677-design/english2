package com.lin.toeic3000.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WordRepository {
    suspend fun load(context: Context): List<Word> = withContext(Dispatchers.IO) {
        context.assets.open("toeic_words.tsv").bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.mapNotNull { line ->
                val parts = line.split('\t', limit = 3)
                val id = parts.getOrNull(0)?.toIntOrNull()
                val english = parts.getOrNull(1)?.trim()
                val chinese = parts.getOrNull(2)?.trim()
                if (id != null && !english.isNullOrBlank() && !chinese.isNullOrBlank()) {
                    Word(id, english, chinese)
                } else null
            }.toList()
        }
    }
}
