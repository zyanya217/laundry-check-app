package io.github.zyanya217.laundrycheck.logic

import io.github.zyanya217.laundrycheck.model.HourlyRow

enum class WeatherIcon { SUN, MOON, CLOUD, RAIN }

// Assumptions carried over from the web app, not yet confirmed against live data: WeatherCode 01 to 03
// mean mostly clear, other codes mean cloudy. Rain is judged from the rain probability, not the code.
fun iconFor(r: HourlyRow): WeatherIcon {
    if (r.pop >= 40) return WeatherIcon.RAIN
    val code = r.code
    if (code != null && code <= 3) {
        val h = wallHour(r.idx)
        return if (h in 6..17) WeatherIcon.SUN else WeatherIcon.MOON
    }
    return WeatherIcon.CLOUD
}
