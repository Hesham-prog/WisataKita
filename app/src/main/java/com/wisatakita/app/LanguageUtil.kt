package com.wisatakita.app

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LanguageUtil {
    private const val PREFS_NAME = "wk_language"
    private const val KEY_LANGUAGE = "language_tag"
    const val INDONESIAN = "id"
    const val ENGLISH = "en"

    fun applySavedLanguage(context: Context) {
        val tag = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, INDONESIAN)
            .orEmpty()
        setAppLocale(tag.ifBlank { INDONESIAN })
    }

    fun setLanguage(context: Context, tag: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, tag)
            .apply()
        setAppLocale(tag)
    }

    fun currentLanguage(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, INDONESIAN) ?: INDONESIAN

    private fun setAppLocale(tag: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }
}
