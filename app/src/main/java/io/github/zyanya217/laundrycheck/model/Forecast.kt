package io.github.zyanya217.laundrycheck.model

/** One response of `/api/forecast` for a single district. */
data class Forecast(
    /** Short-range hourly forecast, about three days. */
    val rows: List<HourlyRow>,
    /** Coarser one-week forecast, used only to fill days the short-range one does not reach. */
    val weekRows: List<HourlyRow>,
    /** When the server fetched it, as an ISO-8601 instant. */
    val fetchedAt: String,
)
