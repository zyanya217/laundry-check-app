package io.github.zyanya217.laundrycheck.data.settings

import io.github.zyanya217.laundrycheck.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<UserSettings>

    suspend fun update(transform: (UserSettings) -> UserSettings)
}
