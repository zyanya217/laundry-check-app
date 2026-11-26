package io.github.zyanya217.laundrycheck.logic

import io.github.zyanya217.laundrycheck.model.HourlyRow

/** Everything the forecast screen shows, derived from the raw hours and the settings. */
data class ForecastContent(
    val window: HangWindow,
    /** Null when the window has no forecast hours. */
    val analysis: Analysis?,
    /** Evenings that have a forecast, for the best-days strip. */
    val picks: List<DayPick>,
    /** Offset of the best available evening, or null when none can be judged. */
    val bestDay: Int?,
    /** True when at least one evening can be judged. */
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
    val merged = mergeRows(rows, weekRows)
    val window = buildWindow(merged, nowIdx, startHour, collectHour)
    val analysis = if (window.rows.isNotEmpty()) analyze(window.rows, covered, window.expectedHours) else null
    val picks = judgedPicks(evaluateWeek(merged, nowIdx, startHour, collectHour, covered))
    return ForecastContent(
        window = window,
        analysis = analysis,
        picks = picks,
        bestDay = bestOffset(picks),
        hasWeek = picks.isNotEmpty(),
    )
}
