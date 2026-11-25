package io.github.zyanya217.laundrycheck.data.forecast

import io.github.zyanya217.laundrycheck.model.Forecast

/**
 * A short code for the screen to show: `network`, `http_<status>`, `unknown`, or the server's own
 * `error` value such as `district_not_found` or `upstream_*`.
 */
class ForecastException(val code: String, cause: Throwable? = null) : Exception(code, cause)

interface ForecastRepository {
    /** Fetches the forecast for one district. Throws [ForecastException] when it cannot. */
    suspend fun forecast(county: String, district: String): Forecast
}
