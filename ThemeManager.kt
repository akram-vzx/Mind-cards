package com.mindcards.app

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {
    private const val PREFS = "mindcards_settings"
    private const val KEY_THEME = "theme"
    private const val KEY_DARK = "dark_mode"
    private const val KEY_LANG = "lang"

    val themes = listOf(
        "blue" to R.style.Theme_MindCards_Blue,
        "purple" to R.style.Theme_MindCards_Purple,
        "green" to R.style.Theme_MindCards_Green,
        "teal" to R.style.Theme_MindCards_Teal,
        "orange" to R.style.Theme_MindCards_Orange,
        "pink" to R.style.Theme_MindCards_Pink
    )

    fun applyTheme(activity: Activity) {
        val key = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_THEME, "blue") ?: "blue"
        val style = themes.firstOrNull { it.first == key }?.second
            ?: R.style.Theme_MindCards_Blue
        activity.setTheme(style)
    }

    fun applyDarkMode(context: Context) {
        val dark = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_DARK, false)
        AppCompatDelegate.setDefaultNightMode(
            if (dark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun setTheme(context: Context, key: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_THEME, key).apply()
    }

    fun setDark(context: Context, dark: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_DARK, dark).apply()
        applyDarkMode(context)
    }

    fun getThemeKey(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_THEME, "blue") ?: "blue"

    fun isDark(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_DARK, false)

    fun getLang(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANG, "ar") ?: "ar"

    fun setLang(context: Context, lang: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANG, lang).apply()
    }
}
