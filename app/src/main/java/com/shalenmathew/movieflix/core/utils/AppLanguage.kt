package com.shalenmathew.movieflix.core.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

object AppLanguage {
    val supported = setOf("en-US", "zh-CN")

    private fun code(locale: Locale?) = if (locale?.language == "zh") "zh-CN" else "en-US"

    /** Use the Activity's rendered configuration for the language picker. */
    fun displayed(context: Context) = code(context.resources.configuration.locales[0])

    /** Application contexts on older Android releases do not inherit AppCompat locales. */
    fun current(context: Context) = AppCompatDelegate.getApplicationLocales()[0]
        ?.let { code(it) } ?: displayed(context)
}
