package io.github.zyanya217.laundrycheck.widget

import io.github.zyanya217.laundrycheck.logic.CollectDay
import io.github.zyanya217.laundrycheck.logic.DRY_OK
import io.github.zyanya217.laundrycheck.logic.Dryness
import io.github.zyanya217.laundrycheck.logic.ForecastContent
import io.github.zyanya217.laundrycheck.logic.Level
import io.github.zyanya217.laundrycheck.logic.wallHour
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** How many evenings the widget shows. The first three that can be judged. */
const val WIDGET_DAYS = 3

/** One evening in the widget's strip. */
data class WidgetDay(
    /** 0 is tonight, 1 is tomorrow night; anything else is printed as a weekday. */
    val offset: Int,
    /** Wall-clock day number (days since the epoch), used to print the weekday. */
    val dayNumber: Int,
    val level: Level,
    val dry: Dryness,
    /** Highest rain probability in that evening's window, in percent. */
    val pop: Int,
    val best: Boolean,
)

sealed interface WidgetData {
    /** No forecast yet, or none that covers the window. */
    data object NoData : WidgetData

    data class Ready(
        val level: Level,
        val dry: Dryness,
        /** Where the dryness meter's fill ends, 0..1. */
        val meterFill: Float,
        /** Day number of the evening being judged, for the date label. */
        val dayNumber: Int,
        val startHour: Int,
        val collectHour: Int,
        val collectDay: CollectDay,
        val days: List<WidgetDay>,
        /** `HH:mm` Taipei time the server fetched the forecast, or empty. */
        val updatedClock: String,
    ) : WidgetData
}

// The meter runs from "still wet" to "dry" on the same scale as the app's VerdictCard.
private const val METER_SCALE = DRY_OK * 1.4

fun meterFill(score: Double): Float = (max(6.0, min(100.0, score / METER_SCALE * 100)) / 100).toFloat()

/** Reduces what the home screen shows to the few values the widget draws. */
fun buildWidgetData(content: ForecastContent, updatedClock: String): WidgetData {
    val analysis = content.analysis ?: return WidgetData.NoData
    val window = content.window
    return WidgetData.Ready(
        level = analysis.level,
        dry = analysis.dry,
        meterFill = meterFill(analysis.score),
        dayNumber = Math.floorDiv(window.startIdx, 24),
        startHour = wallHour(window.startIdx),
        collectHour = wallHour(window.endIdx),
        collectDay = window.collectDay,
        days = content.picks.take(WIDGET_DAYS).mapNotNull { pick ->
            val a = pick.analysis ?: return@mapNotNull null
            WidgetDay(
                offset = pick.offset,
                dayNumber = pick.dayNumber,
                level = a.level,
                dry = a.dry,
                pop = pick.maxPop.roundToInt(),
                best = pick.offset == content.bestDay,
            )
        },
        updatedClock = updatedClock,
    )
}
