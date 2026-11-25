package io.github.zyanya217.laundrycheck.ui.settings

import android.app.LocaleManager
import android.content.Context
import android.os.LocaleList
import java.util.Locale

/**
 * The in-app language switch uses the platform's per-app language (API 33), which the system also shows
 * in Settings > Apps > Laundry Check > Language. The system stores the choice and recreates the activity,
 * so nothing is kept in [io.github.zyanya217.laundrycheck.data.settings.SettingsRepository].
 * The languages offered must match res/xml/locales_config.xml.
 */
enum class AppLanguage(val tag: String, val label: String) {
    TRADITIONAL_CHINESE("zh-TW", "繁體中文"),
    ENGLISH("en", "English"),
}

fun currentAppLanguage(context: Context): AppLanguage {
    val chosen = context.getSystemService(LocaleManager::class.java).applicationLocales
    val language = if (chosen.isEmpty) Locale.getDefault().language else chosen[0].language
    // The app's default strings are Traditional Chinese, so anything else that is not English shows those.
    return if (language == "en") AppLanguage.ENGLISH else AppLanguage.TRADITIONAL_CHINESE
}

fun setAppLanguage(context: Context, language: AppLanguage) {
    context.getSystemService(LocaleManager::class.java).applicationLocales = LocaleList.forLanguageTags(language.tag)
}
