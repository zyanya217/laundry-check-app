package io.github.zyanya217.laundrycheck.ui.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.zyanya217.laundrycheck.data.forecast.ForecastException
import io.github.zyanya217.laundrycheck.data.forecast.ForecastRepository
import io.github.zyanya217.laundrycheck.data.settings.SettingsRepository
import io.github.zyanya217.laundrycheck.logic.ForecastContent
import io.github.zyanya217.laundrycheck.logic.buildForecastContent
import io.github.zyanya217.laundrycheck.logic.taipeiClock
import io.github.zyanya217.laundrycheck.logic.taipeiNowIdx
import io.github.zyanya217.laundrycheck.model.Forecast
import io.github.zyanya217.laundrycheck.model.UserSettings
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FetchStatus { LOADING, READY, ERROR }

data class ForecastUiState(
    val settings: UserSettings = UserSettings(),
    val status: FetchStatus = FetchStatus.LOADING,
    /** Short code of the last failure, shown under the error message. */
    val errorCode: String = "",
    /** Null until a forecast for the current district has arrived. */
    val content: ForecastContent? = null,
    /** `HH:mm` Taipei time the server fetched the forecast, or empty. */
    val updatedClock: String = "",
)

@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val forecasts: ForecastRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
) : ViewModel() {

    private data class Fetch(
        val status: FetchStatus = FetchStatus.LOADING,
        val forecast: Forecast? = null,
        val location: Pair<String, String>? = null,
        val errorCode: String = "",
        val loadedAtMs: Long = 0,
    )

    private val fetch = MutableStateFlow(Fetch())
    private val reloads = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Re-evaluates "now" every minute, but only while something is collecting [uiState]. */
    private val ticker: Flow<Instant> = flow {
        while (true) {
            emit(clock.instant())
            delay(TICK_MS)
        }
    }

    val uiState: StateFlow<ForecastUiState> =
        combine(settingsRepository.settings, fetch, ticker) { settings, f, now ->
            val nowIdx = taipeiNowIdx(now)
            ForecastUiState(
                settings = settings,
                status = f.status,
                errorCode = f.errorCode,
                content = f.forecast?.let {
                    buildForecastContent(it.rows, it.weekRows, nowIdx, settings.startHour, settings.collectHour, settings.covered)
                },
                updatedClock = f.forecast?.let { taipeiClock(it.fetchedAt) }.orEmpty(),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ForecastUiState())

    init {
        viewModelScope.launch {
            // A new district or a reload request cancels the load in flight and starts another.
            settingsRepository.settings
                .map { it.county to it.district }
                .distinctUntilChanged()
                .combine(reloads.onStart { emit(Unit) }) { location, _ -> location }
                .collectLatest { (county, district) -> load(county, district) }
        }
    }

    private suspend fun load(county: String, district: String) {
        val here = county to district
        // Keep the old forecast on screen while reloading the same district; drop it for a different one.
        fetch.update { f ->
            f.copy(
                status = FetchStatus.LOADING,
                errorCode = "",
                forecast = f.forecast.takeIf { f.location == here },
                location = here,
            )
        }
        try {
            val forecast = forecasts.forecast(county, district)
            fetch.value = Fetch(FetchStatus.READY, forecast, here, "", clock.millis())
        } catch (e: ForecastException) {
            fetch.update { it.copy(status = FetchStatus.ERROR, errorCode = e.code) }
        }
    }

    fun reload() {
        reloads.tryEmit(Unit)
    }

    /** Called when the app comes to the foreground: reload a forecast older than 15 minutes. */
    fun refreshIfStale() {
        val f = fetch.value
        if (f.status == FetchStatus.READY && clock.millis() - f.loadedAtMs > STALE_MS) reload()
    }

    fun setStartHour(hour: Int) {
        viewModelScope.launch { settingsRepository.update { it.copy(startHour = hour) } }
    }

    private companion object {
        const val TICK_MS = 60_000L
        const val STALE_MS = 15 * 60 * 1000L
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
