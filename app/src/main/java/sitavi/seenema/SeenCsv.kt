/* Based on Seenema, Copyright (C) 2026 Sitavi. SPDX-License-Identifier: GPL-3.0-or-later */
package sitavi.seenema

object SeenCsv {
    /** Parses current and older CSV layouts; rows it can't understand are skipped. */
    fun decode(text: String): List<SeenItem> =
        parseCsv(text).drop(1).mapNotNull { row ->
            when {
                // id,type,title,year,poster_url,status,rating,note,added_at
                row.size >= 9 && row[0].isNotBlank() -> SeenItem(
                    row[0], row[1], row[2], row[3], row[4],
                    if (row[5] == Status.WATCHLIST) Status.WATCHLIST else Status.SEEN,
                    row[6].toIntOrNull() ?: 0, row[7], row[8],
                )
                // id,type,title,year,poster_url,rating,added_at
                row.size == 7 && row[0].isNotBlank() -> SeenItem(
                    row[0], row[1], row[2], row[3], row[4],
                    Status.SEEN, row[5].toIntOrNull() ?: 0, "", row[6],
                )
                // id,type,title,year,poster_url,added_at
                row.size == 6 && row[0].isNotBlank() -> SeenItem(
                    row[0], row[1], row[2], row[3], row[4], Status.SEEN, 0, "", row[5],
                )
                else -> null
            }
        }

    fun encode(items: List<SeenItem>): String = buildString {
        append("id,type,title,year,poster_url,status,rating,note,added_at\r\n")
        for (item in items) append(listOf(item.id, item.mediaType, item.title, item.year,
            item.posterUrl, item.status, if (item.rating > 0) item.rating.toString() else "",
            item.note, item.addedAt).joinToString(",") { esc(it) }).append("\r\n")
    }

    private fun esc(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' })
            "\"" + value.replace("\"", "\"\"") + "\""
        else value

    private fun parseCsv(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (inQuotes) {
                when {
                    c == '"' && i + 1 < text.length && text[i + 1] == '"' -> { field.append('"'); i++ }
                    c == '"' -> inQuotes = false
                    else -> field.append(c)
                }
            } else when (c) {
                '"' -> inQuotes = true
                ',' -> { row.add(field.toString()); field.setLength(0) }
                '\r' -> {}
                '\n' -> { row.add(field.toString()); field.setLength(0); rows.add(row); row = mutableListOf() }
                else -> field.append(c)
            }
            i++
        }
        if (field.isNotEmpty() || row.isNotEmpty()) { row.add(field.toString()); rows.add(row) }
        return rows
    }
}
