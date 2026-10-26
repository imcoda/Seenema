package com.shalenmathew.movieflix.core.utils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
object DiaryDate {
    // Keep the existing timestamp column and backup format; only the month is shown.
    fun watchedMonthTimestamp(year: Int, month: Int): String {
        require(year in 1..9999 && month in 1..12)
        return String.format(Locale.ROOT, "%04d-%02d-01 12:00:00", year, month)
    }
    fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
    fun valid(text: String): Boolean = text.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}")) && runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { isLenient=false }.parse(text) != null
    }.getOrDefault(false)
    fun month(date: String?) = date?.take(7)?.takeIf { it.matches(Regex("[0-9]{4}-[0-9]{2}")) } ?: "未注明日期"
}
