package io.github.zyanya217.laundrycheck.logic

import io.github.zyanya217.laundrycheck.model.HourlyRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HangWindowTest {
    private val day20 = 24 * 20
    private val rows = List(72) { i -> HourlyRow(idx = day20 + i, temp = 20.0, rh = 70.0, wind = 2.0, pop = 10.0, code = 4) }

    @Test
    fun runsFromStartHourTonightToCollectHourTomorrowInclusive() {
        val w = buildWindow(rows, day20 + 13, 21, 8)
        assertEquals(day20 + 21, w.startIdx)
        assertEquals(day20 + 32, w.endIdx)
        assertEquals(12, w.rows.size)
        assertEquals(CollectDay.TOMORROW, w.collectDay)
        assertFalse(w.startMoved)
    }

    @Test
    fun startsAtNextFullHourWhenChosenStartHasPassed() {
        val w = buildWindow(rows, day20 + 22, 21, 8)
        assertEquals(day20 + 23, w.startIdx)
        assertTrue(w.startMoved)
    }

    @Test
    fun keepsOnlyTheHoursTheForecastCovers() {
        val short = rows.filter { it.idx <= day20 + 26 }
        assertEquals(6, buildWindow(short, day20 + 13, 21, 8).rows.size)
    }

    @Test
    fun hourlySlotsRetainGapsAtTheBeginningMiddleAndEnd() {
        val sparse = listOf(rows[22], rows[24])
        val window = buildWindow(sparse, day20 + 13, 21, 8)
        assertEquals(12, window.expectedHours)
        assertEquals(listOf(null, day20 + 22, null, day20 + 24) + List(8) { null }, window.hourlySlots().map { it?.idx })
    }

    @Test
    fun collectsTheSameDayWhenStartIsAfterMidnight() {
        val w = buildWindow(rows, day20 + 1, 3, 8)
        assertEquals(CollectDay.TODAY, w.collectDay)
    }

    @Test
    fun movesTheWholeWindowForwardByDayOffset() {
        val w = buildWindow(rows, day20 + 13, 21, 8, dayOffset = 1)
        assertEquals(day20 + 24 + 21, w.startIdx)
        assertEquals(day20 + 24 + 32, w.endIdx)
        assertEquals(CollectDay.DAY_AFTER_TOMORROW, w.collectDay)
        assertFalse(w.startMoved)
    }

    private fun row(idx: Int, temp: Double) = HourlyRow(idx, temp, rh = 70.0, wind = 2.0, pop = 10.0, code = 4)

    @Test
    fun mergeKeepsEveryHourFromBothListsInOrderPreferringPrimary() {
        val merged = mergeRows(
            primary = listOf(row(2, 99.0), row(3, 99.0)),
            fallback = listOf(row(1, 1.0), row(2, 1.0), row(4, 1.0)),
        )
        assertEquals(listOf(1, 2, 3, 4), merged.map { it.idx })
        assertEquals(listOf(1.0, 99.0, 99.0, 1.0), merged.map { it.temp })
    }

    @Test
    fun mergeWorksWhenOneListIsEmpty() {
        assertEquals(1, mergeRows(emptyList(), listOf(row(1, 1.0))).size)
        assertEquals(1, mergeRows(listOf(row(1, 1.0)), emptyList()).size)
    }
}
