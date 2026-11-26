package io.github.zyanya217.laundrycheck.widget

import io.github.zyanya217.laundrycheck.data.forecast.ForecastException
import io.github.zyanya217.laundrycheck.data.forecast.ForecastRepository
import io.github.zyanya217.laundrycheck.data.settings.SettingsRepository
import io.github.zyanya217.laundrycheck.logic.buildForecastContent
import io.github.zyanya217.laundrycheck.logic.taipeiClock
import io.github.zyanya217.laundrycheck.logic.taipeiNowIdx
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** Fetches for, and reads from, the widget's own cache. Uses the same settings and logic as the app screen. */
class WidgetDataSource @Inject constructor(
    private val forecasts: ForecastRepository,
    private val settingsRepository: SettingsRepository,
    private val cache: ForecastCache,
    private val clock: Clock,
) {
    /** Fetches the forecast for the chosen district into the cache. Returns false when it could not. */
    suspend fun refresh(): Boolean {
        val s = settingsRepository.settings.first()
        return try {
            cache.save(s.county, s.district, forecasts.forecast(s.county, s.district))
            true
        } catch (_: ForecastException) {
            false
        }
    }

    /** What the widget draws right now, worked out from the cached forecast and the current time. */
    suspend fun snapshot(): WidgetData {
        val s = settingsRepository.settings.first()
        val forecast = cache.load(s.county, s.district) ?: return WidgetData.NoData
        val content = buildForecastContent(
            rows = forecast.rows,
            weekRows = forecast.weekRows,
            nowIdx = taipeiNowIdx(clock.instant()),
            startHour = s.startHour,
            collectHour = s.collectHour,
            covered = s.covered,
        )
        return buildWidgetData(content, taipeiClock(forecast.fetchedAt))
    }
}
