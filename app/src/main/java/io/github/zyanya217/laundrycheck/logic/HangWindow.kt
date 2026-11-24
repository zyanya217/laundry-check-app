package io.github.zyanya217.laundrycheck.logic

import io.github.zyanya217.laundrycheck.model.HourlyRow
import kotlin.math.max

/** Which day the clothes come in, relative to now. */
enum class CollectDay { TODAY, TOMORROW, DAY_AFTER_TOMORROW }

data class HangWindow(
    val rows: List<HourlyRow>,
    val startIdx: Int,
    val endIdx: Int,
    val collectDay: CollectDay,
    /** True when the chosen start hour had already passed and the window begins at the next full hour. */
    val startMoved: Boolean,
)

/** Combines two forecasts by hour, ordered by hour. Where both have an hour, [primary] wins. */
fun mergeRows(primary: List<HourlyRow>, fallback: List<HourlyRow>): List<HourlyRow> {
    val byIdx = HashMap<Int, HourlyRow>()
    fallback.forEach { byIdx[it.idx] = it }
    primary.forEach { byIdx[it.idx] = it }
    return byIdx.values.sortedBy { it.idx }
}

/**
 * Hours from the start of hanging up to (and including) the collect hour.
 * [dayOffset] moves the window forward by whole days (0 is tonight, 1 is tomorrow night, ...).
 */
fun buildWindow(
    rows: List<HourlyRow>,
    nowIdx: Int,
    startHour: Int,
    collectHour: Int,
    dayOffset: Int = 0,
): HangWindow {
    val wanted = Math.floorDiv(nowIdx, 24) * 24 + dayOffset * 24 + startHour
    val startIdx = max(wanted, nowIdx + 1)
    var endIdx = Math.floorDiv(startIdx, 24) * 24 + collectHour
    if (endIdx <= startIdx) endIdx += 24

    val byIdx = rows.associateBy { it.idx }
    val inWindow = (startIdx..endIdx).mapNotNull { byIdx[it] }

    val dayDiff = Math.floorDiv(endIdx, 24) - Math.floorDiv(nowIdx, 24)
    return HangWindow(
        rows = inWindow,
        startIdx = startIdx,
        endIdx = endIdx,
        collectDay = when {
            dayDiff <= 0 -> CollectDay.TODAY
            dayDiff == 1 -> CollectDay.TOMORROW
            else -> CollectDay.DAY_AFTER_TOMORROW
        },
        startMoved = startIdx > wanted,
    )
}
