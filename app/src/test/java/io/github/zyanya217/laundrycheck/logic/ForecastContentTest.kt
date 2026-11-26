package io.github.zyanya217.laundrycheck.logic

import io.github.zyanya217.laundrycheck.model.HourlyRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ForecastContentTest {
    private val midnight = 480
    private val now = midnight + 13

    private fun clear(idx: Int) = HourlyRow(idx, temp = 22.0, rh = 60.0, wind = 3.0, pop = 3.0, code = 1)

    private fun hours(days: Int, from: Int = midnight) = List(days * 24) { clear(from + it) }

    @Test
    fun judgesTonightAndRecommendsAnEvening() {
        val c = buildForecastContent(hours(9), emptyList(), now, 21, 8, covered = false)
        assertEquals(Level.OK, c.analysis?.level)
        assertEquals(0, c.bestDay)
        assertTrue(c.hasWeek)
        assertEquals(4, c.picks.size)
    }

    @Test
    fun estimatesFromOneHourAndIncludesItInRecommendations() {
        val c = buildForecastContent(listOf(clear(now + 8)), emptyList(), now, 21, 8, covered = false)
        assertNotNull(c.analysis)
        assertEquals(0, c.bestDay)
        assertTrue(c.hasWeek)
        assertEquals(evaporation(clear(now + 8), false) * 12, c.analysis!!.score, 1e-10)
    }

    @Test
    fun noHoursCannotProduceAnEstimate() {
        val c = buildForecastContent(emptyList(), emptyList(), now, 21, 8, covered = false)
        assertNull(c.analysis)
        assertNull(c.bestDay)
        assertFalse(c.hasWeek)
    }

    @Test
    fun shortForecastAndWeekForecastFillEachOthersGaps() {
        val short = listOf(clear(midnight + 21).copy(pop = 80.0))
        val week = listOf(clear(midnight + 21), clear(midnight + 23))
        val c = buildForecastContent(short, week, now, 21, 8, covered = false)
        assertEquals(listOf(midnight + 21, midnight + 23), c.window.rows.map { it.idx })
        assertEquals(Level.BAD, c.analysis?.level)
        assertEquals(c.analysis, c.picks.single().analysis)
    }

    @Test
    fun weekRowsFillDaysBeyondTheShortRangeForecast() {
        val short = hours(2)
        val week = List(9 * 24) { clear(midnight + it) }.filter { it.idx >= midnight + 48 }
        val c = buildForecastContent(short, week, now, 21, 8, covered = false)
        assertEquals(4, c.picks.size)
        assertNotNull(c.picks.last().analysis)
    }

    @Test
    fun showsTheStripWhenOnlyTonightCanBeJudged() {
        val c = buildForecastContent(hours(2).filter { it.idx <= midnight + 32 }, emptyList(), now, 21, 8, covered = false)
        assertEquals(1, c.picks.size)
        assertTrue(c.hasWeek)
    }
}
