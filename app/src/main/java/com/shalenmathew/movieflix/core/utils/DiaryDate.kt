package com.shalenmathew.movieflix.core.utils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
object DiaryDate {
    fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
    fun valid(text: String): Boolean = text.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}")) && runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { isLenient=false }.parse(text) != null
    }.getOrDefault(false)
    fun month(date: String?) = date?.take(7)?.takeIf { it.matches(Regex("[0-9]{4}-[0-9]{2}")) } ?: "未注明日期"
}
