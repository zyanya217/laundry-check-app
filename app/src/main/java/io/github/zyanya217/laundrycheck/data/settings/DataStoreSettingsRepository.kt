package io.github.zyanya217.laundrycheck.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.zyanya217.laundrycheck.model.ThemeChoice
import io.github.zyanya217.laundrycheck.model.UserSettings
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val settings: Flow<UserSettings> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it.toSettings() }

    override suspend fun update(transform: (UserSettings) -> UserSettings) {
        dataStore.edit { prefs -> prefs.write(transform(prefs.toSettings())) }
    }

    private companion object {
        val COUNTY = stringPreferencesKey("county")
        val DISTRICT = stringPreferencesKey("district")
        val START_HOUR = intPreferencesKey("start_hour")
        val COLLECT_HOUR = intPreferencesKey("collect_hour")
        val COVERED = booleanPreferencesKey("covered")
        val NOTIFY_ENABLED = booleanPreferencesKey("notify_enabled")
        val NOTIFY_HOUR = intPreferencesKey("notify_hour")
        val THEME = stringPreferencesKey("theme")
    }

    // A missing or unreadable value falls back to the default, so old or edited data never crashes the app.
    private fun Preferences.toSettings(): UserSettings {
        val d = UserSettings()
        return UserSettings(
            county = this[COUNTY] ?: d.county,
            district = this[DISTRICT] ?: d.district,
            startHour = this[START_HOUR] ?: d.startHour,
            collectHour = this[COLLECT_HOUR] ?: d.collectHour,
            covered = this[COVERED] ?: d.covered,
            notifyEnabled = this[NOTIFY_ENABLED] ?: d.notifyEnabled,
            notifyHour = this[NOTIFY_HOUR] ?: d.notifyHour,
            theme = this[THEME]?.let { name -> ThemeChoice.entries.firstOrNull { it.name == name } } ?: d.theme,
        )
    }

    private fun MutablePreferences.write(s: UserSettings) {
        this[COUNTY] = s.county
        this[DISTRICT] = s.district
        this[START_HOUR] = s.startHour
        this[COLLECT_HOUR] = s.collectHour
        this[COVERED] = s.covered
        this[NOTIFY_ENABLED] = s.notifyEnabled
        this[NOTIFY_HOUR] = s.notifyHour
        this[THEME] = s.theme.name
    }
}
