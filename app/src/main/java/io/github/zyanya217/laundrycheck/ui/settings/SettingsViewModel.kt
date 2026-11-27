package io.github.zyanya217.laundrycheck.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.zyanya217.laundrycheck.data.district.DistrictRepository
import io.github.zyanya217.laundrycheck.data.location.LocationException
import io.github.zyanya217.laundrycheck.data.location.LocationFailure
import io.github.zyanya217.laundrycheck.data.location.LocationProvider
import io.github.zyanya217.laundrycheck.data.settings.SettingsRepository
import io.github.zyanya217.laundrycheck.logic.MAX_GPS_KM
import io.github.zyanya217.laundrycheck.logic.nearestDistrict
import io.github.zyanya217.laundrycheck.model.DistrictEntry
import io.github.zyanya217.laundrycheck.model.ThemeChoice
import io.github.zyanya217.laundrycheck.model.UserSettings
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** What the "use my location" button reports back. */
sealed interface GpsMessage {
    data class Located(val county: String, val district: String) : GpsMessage
    data object Denied : GpsMessage
    data object Unavailable : GpsMessage
    data object Timeout : GpsMessage
    data object Outside : GpsMessage
}

data class GpsState(val busy: Boolean = false, val message: GpsMessage? = null)

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val counties: List<String> = emptyList(),
    val districts: List<String> = emptyList(),
    val gps: GpsState = GpsState(),
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val districtRepository: DistrictRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val table = MutableStateFlow<Map<String, List<DistrictEntry>>>(emptyMap())
    private val gps = MutableStateFlow(GpsState())

    val uiState: StateFlow<SettingsUiState> =
        combine(settingsRepository.settings, table, gps) { settings, table, gps ->
            // The saved choice stays selectable even if it is missing from the file.
            val counties = table.keys.toList().let { if (settings.county in it) it else listOf(settings.county) + it }
            val districts = table[settings.county].orEmpty().map { it.name }
                .let { if (settings.district in it) it else listOf(settings.district) + it }
            SettingsUiState(settings, counties, districts, gps)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    init {
        viewModelScope.launch { table.value = districtRepository.load() }
    }

    fun onCounty(county: String) {
        val first = table.value[county]?.firstOrNull()?.name.orEmpty()
        update { it.copy(county = county, district = first) }
        gps.value = GpsState()
    }

    fun onDistrict(district: String) = update { it.copy(district = district) }

    fun onStartHour(hour: Int) = update { it.copy(startHour = hour) }

    fun onCollectHour(hour: Int) = update { it.copy(collectHour = hour) }

    fun onCovered(covered: Boolean) = update { it.copy(covered = covered) }

    fun onNotifyEnabled(enabled: Boolean) = update { it.copy(notifyEnabled = enabled) }

    fun onNotifyHour(hour: Int) = update { it.copy(notifyHour = hour) }

    fun onTheme(theme: ThemeChoice) = update { it.copy(theme = theme) }

    /** The screen asks for the permission first and calls this only once it is granted. */
    fun useMyLocation() {
        viewModelScope.launch {
            gps.value = GpsState(busy = true)
            gps.value = GpsState(message = findDistrict())
        }
    }

    fun onPermissionDenied() {
        gps.value = GpsState(message = GpsMessage.Denied)
    }

    private suspend fun findDistrict(): GpsMessage {
        val position = try {
            locationProvider.current()
        } catch (e: LocationException) {
            return when (e.failure) {
                LocationFailure.DENIED -> GpsMessage.Denied
                LocationFailure.TIMEOUT -> GpsMessage.Timeout
                LocationFailure.UNAVAILABLE -> GpsMessage.Unavailable
            }
        }
        val hit = nearestDistrict(position.lat, position.lon, table.value) ?: return GpsMessage.Unavailable
        if (hit.km > MAX_GPS_KM) return GpsMessage.Outside
        update { it.copy(county = hit.county, district = hit.district) }
        return GpsMessage.Located(hit.county, hit.district)
    }

    private fun update(transform: (UserSettings) -> UserSettings) {
        viewModelScope.launch { settingsRepository.update(transform) }
    }
}
