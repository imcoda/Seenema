/*
 * Seenema: track and rate the films and series you have watched.
 * Copyright (C) 2026 Sitavi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package sitavi.seenema

import android.content.Context
import android.util.AtomicFile
import java.io.File

object Status {
    const val SEEN = "seen"
    const val WATCHLIST = "watchlist"
}

data class SeenItem(
    val id: String, // IMDb id, e.g. "tt0133093"
    val mediaType: String, // "movie" or "tv"
    val title: String,
    val year: String,
    val posterUrl: String,
    val status: String, // Status.SEEN or Status.WATCHLIST
    val rating: Int, // 1..5 stars, 0 if unrated / watchlist
    val note: String,
    val addedAt: String, // ISO-8601 local date-time, sorts lexicographically
)

/**
 * Persists the list as a CSV in Android/media/sitavi.seenema/
 * (user-visible through any file manager, like WhatsApp's media folder).
 */
class SeenStore(private val context: Context) {

    @Suppress("DEPRECATION")
    val file: File by lazy {
        val dir = context.externalMediaDirs.firstOrNull()?.takeIf { it.exists() || it.mkdirs() }
            ?: context.filesDir
        File(dir, "seenema.csv")
    }

    fun load(): List<SeenItem> {
        if (!file.exists()) return emptyList()
        return try {
            parseItems(AtomicFile(file).readFully().toString(Charsets.UTF_8))
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun parseItems(text: String) = SeenCsv.decode(text)
    fun csv(items: List<SeenItem>) = SeenCsv.encode(items)
    @Synchronized fun save(items: List<SeenItem>) {
        val atomic = AtomicFile(file)
        val output = atomic.startWrite()
        try { output.write(csv(items).toByteArray(Charsets.UTF_8)); atomic.finishWrite(output) }
        catch (e: Exception) { atomic.failWrite(output); throw e }
    }
}
