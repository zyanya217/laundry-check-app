package io.github.zyanya217.laundrycheck.data.forecast

import io.github.zyanya217.laundrycheck.model.Forecast
import io.github.zyanya217.laundrycheck.model.HourlyRow
import kotlinx.serialization.Serializable

/** Wire format of `GET /api/forecast`, as the web app's `ForecastPayload` describes it. */
@Serializable
data class ForecastResponse(
    val county: String,
    val district: String,
    val rows: List<HourlyRowDto>,
    // Missing or empty when the server could not read the one-week forecast.
    val weekRows: List<HourlyRowDto> = emptyList(),
    val fetchedAt: String,
)

@Serializable
data class HourlyRowDto(
    val idx: Int,
    val temp: Double,
    val rh: Double,
    val wind: Double,
    val pop: Double,
    val code: Int? = null,
)

/** Body of a non-2xx response: `{ "error": "<code>" }`. */
@Serializable
data class ErrorBody(val error: String)

fun HourlyRowDto.toModel() = HourlyRow(idx = idx, temp = temp, rh = rh, wind = wind, pop = pop, code = code)

fun ForecastResponse.toModel() = Forecast(
    rows = rows.map { it.toModel() },
    weekRows = weekRows.map { it.toModel() },
    fetchedAt = fetchedAt,
)
