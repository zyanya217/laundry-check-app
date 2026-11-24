package io.github.zyanya217.laundrycheck.logic

import io.github.zyanya217.laundrycheck.model.HourlyRow

/**
 * How many evenings to try. The short-range forecast only reaches about three to four days (it grows
 * through the day as the forecast is reissued), and the one-week dataset has no rain probability past
 * that, so the last ones are often unjudged.
 */
const val WEEK_DAYS = 4

/** A day is only judged when at least this share of its hours has forecast data. */
private const val MIN_COVERAGE = 0.6

data class DayPick(
    val offset: Int,
    /** Wall-clock day number (days since the epoch), used to print the date. */
    val dayNumber: Int,
    val analysis: Analysis?,
    /** Highest rain probability in the window, for display. */
    val maxPop: Double,
)

/** Judges each of the next evenings with the same window the main verdict uses (start hour to collect hour). */
fun evaluateWeek(
    rows: List<HourlyRow>,
    nowIdx: Int,
    startHour: Int,
    collectHour: Int,
    covered: Boolean,
): List<DayPick> =
    (0 until WEEK_DAYS).map { offset ->
        val w = buildWindow(rows, nowIdx, startHour, collectHour, offset)
        val expected = w.endIdx - w.startIdx + 1
        val enough = w.rows.size >= 2 && w.rows.size >= expected * MIN_COVERAGE
        DayPick(
            offset = offset,
            dayNumber = Math.floorDiv(w.startIdx, 24),
            analysis = if (enough) analyze(w.rows, covered) else null,
            maxPop = if (enough) w.rows.maxOf { it.pop } else 0.0,
        )
    }

private val levelRank = mapOf(Level.OK to 0, Level.MID to 1, Level.BAD to 2)

/**
 * The offset of the best evening, or null when no evening is good enough to recommend.
 * Better verdict first, then the higher dryness score, then the earlier day.
 */
fun bestOffset(picks: List<DayPick>): Int? {
    val judged = picks.mapNotNull { p -> p.analysis?.let { p to it } }
    val best = judged.minWithOrNull(
        compareBy<Pair<DayPick, Analysis>> { levelRank.getValue(it.second.level) }
            .thenByDescending { it.second.score }
            .thenBy { it.first.offset },
    ) ?: return null
    return if (best.second.level == Level.BAD) null else best.first.offset
}

/** Drops the trailing days that could not be judged, so the list only has days with a forecast. */
fun judgedPicks(picks: List<DayPick>): List<DayPick> = picks.dropLastWhile { it.analysis == null }
