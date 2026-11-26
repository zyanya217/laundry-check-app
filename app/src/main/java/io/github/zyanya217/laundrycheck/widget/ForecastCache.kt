package io.github.zyanya217.laundrycheck.widget

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.zyanya217.laundrycheck.data.forecast.ForecastResponse
import io.github.zyanya217.laundrycheck.data.forecast.HourlyRowDto
import io.github.zyanya217.laundrycheck.data.forecast.toModel
import io.github.zyanya217.laundrycheck.model.Forecast
import io.github.zyanya217.laundrycheck.model.HourlyRow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * The last forecast the widget fetched, so it still shows something while the phone is offline.
 * The app screen itself keeps no cache; this one is for the widget only.
 */
@Singleton
class ForecastCache @Inject constructor(
    @ApplicationContext context: Context,
    private val json: Json,
) {
    private val prefs = context.getSharedPreferences("widget_forecast", Context.MODE_PRIVATE)

    fun save(county: String, district: String, forecast: Forecast) {
        val body = ForecastResponse(
            county = county,
            district = district,
            rows = forecast.rows.map { it.toDto() },
            weekRows = forecast.weekRows.map { it.toDto() },
            fetchedAt = forecast.fetchedAt,
        )
        prefs.edit().putString(KEY, json.encodeToString(body)).apply()
    }

    /** The cached forecast, only when it was fetched for this district. */
    fun load(county: String, district: String): Forecast? {
        val text = prefs.getString(KEY, null) ?: return null
        val body = try {
            json.decodeFromString<ForecastResponse>(text)
        } catch (_: SerializationException) {
            return null
        }
        return if (body.county == county && body.district == district) body.toModel() else null
    }

    private fun HourlyRow.toDto() = HourlyRowDto(idx = idx, temp = temp, rh = rh, wind = wind, pop = pop, code = code)

    private companion object {
        const val KEY = "forecast"
    }
}
