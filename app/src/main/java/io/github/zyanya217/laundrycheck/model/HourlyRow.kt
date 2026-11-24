package io.github.zyanya217.laundrycheck.model

/**
 * One forecast hour, as returned by the server's `/api/forecast`.
 *
 * [idx] counts hours since the epoch on the Taipei wall clock (UTC+8, no daylight saving), so
 * `idx % 24` is the Taipei hour of day. [pop] is the probability of precipitation in percent.
 */
data class HourlyRow(
    val idx: Int,
    val temp: Double,
    val rh: Double,
    val wind: Double,
    val pop: Double,
    val code: Int? = null,
)
