package io.github.zyanya217.laundrycheck.logic

import io.github.zyanya217.laundrycheck.model.HourlyRow

/** Everything the forecast screen shows, derived from the raw hours and the settings. */
data class ForecastContent(
    val window: HangWindow,
    /** Null when the window has fewer than two forecast hours ("not enough data"). */
    val analysis: Analysis?,
    /** Evenings that have a forecast, for the best-days strip. */
    val picks: List<DayPick>,
    /** Offset of the recommended evening, or null when none is good enough. */
    val bestDay: Int?,
    /** True when at least one evening after tonight could be judged, so the strip is worth showing. */
    val hasWeek: Boolean,
)

/** The composition done in the web app's App.vue. */
fun buildForecastContent(
    rows: List<HourlyRow>,
    weekRows: List<HourlyRow>,
    nowIdx: Int,
    startHour: Int,
    collectHour: Int,
    covered: Boolean,
): ForecastContent {
    val window = buildWindow(rows, nowIdx, startHour, collectHour)
    val analysis = if (window.rows.size >= 2) analyze(window.rows, covered) else null
    // Short-range hours win; the one-week forecast fills the days beyond them.
    val picks = judgedPicks(evaluateWeek(mergeRows(rows, weekRows), nowIdx, startHour, collectHour, covered))
    return ForecastContent(
        window = window,
        analysis = analysis,
        picks = picks,
        bestDay = bestOffset(picks),
        hasWeek = picks.any { it.offset > 0 },
    )
}
