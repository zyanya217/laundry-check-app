package io.github.zyanya217.laundrycheck.logic

import io.github.zyanya217.laundrycheck.model.HourlyRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeekPlannerTest {
    // idx 480 is a Taipei midnight; "now" is 13:00 on that day.
    private val midnight = 480
    private val now = midnight + 13

    private enum class Mode { CLEAR, RAIN, HUMID }

    private fun hourRow(idx: Int, mode: Mode) = when (mode) {
        Mode.RAIN -> HourlyRow(idx, temp = 20.0, rh = 95.0, wind = 1.0, pop = 85.0, code = 8)
        Mode.HUMID -> HourlyRow(idx, temp = 17.0, rh = 95.0, wind = 1.0, pop = 10.0, code = 7)
        Mode.CLEAR -> HourlyRow(idx, temp = 22.0, rh = 60.0, wind = 3.0, pop = 3.0, code = 1)
    }

    /** Hourly rows where each calendar day follows its own mode. */
    private fun week(modes: List<Mode>): List<HourlyRow> =
        List(modes.size * 24) { i -> hourRow(midnight + i, modes[i / 24]) }

    private fun all(mode: Mode, days: Int) = List(days) { mode }

    @Test
    fun judgesFourEveningsEachFromStartHourToNextMorning() {
        val picks = evaluateWeek(week(all(Mode.CLEAR, 9)), now, 21, 8, covered = false)
        assertEquals(4, picks.size)
        assertEquals(listOf(0, 1, 2, 3), picks.map { it.offset })
        assertTrue(picks.all { it.analysis?.level == Level.OK })
        assertEquals(1, picks[1].dayNumber - picks[0].dayNumber)
    }

    @Test
    fun leavesADayWithoutEnoughForecastUnjudged() {
        val picks = evaluateWeek(week(all(Mode.CLEAR, 3)), now, 21, 8, covered = false)
        assertNotNull(picks[0].analysis)
        assertNotNull(picks[1].analysis)
        assertNull(picks[2].analysis)
        assertNull(picks[3].analysis)
    }

    @Test
    fun showsOnlyTheDaysThatHaveAForecast() {
        assertEquals(2, judgedPicks(evaluateWeek(week(all(Mode.CLEAR, 3)), now, 21, 8, false)).size)
        assertEquals(3, judgedPicks(evaluateWeek(week(all(Mode.CLEAR, 4)), now, 21, 8, false)).size)
        assertEquals(4, judgedPicks(evaluateWeek(week(all(Mode.CLEAR, 5)), now, 21, 8, false)).size)
        assertEquals(emptyList<DayPick>(), judgedPicks(emptyList()))
    }

    @Test
    fun aRainyDaySpoilsItsOwnEveningAndTheEveningBefore() {
        // Day 3 rains. The evening of day 2 ends in day 3's morning rain, and the evening of day 3 is itself rainy.
        val modes = listOf(
            Mode.CLEAR, Mode.CLEAR, Mode.CLEAR, Mode.RAIN, Mode.CLEAR, Mode.CLEAR, Mode.CLEAR, Mode.CLEAR, Mode.CLEAR,
        )
        val picks = evaluateWeek(week(modes), now, 21, 8, false)
        assertEquals(listOf(Level.OK, Level.OK, Level.BAD, Level.BAD), picks.map { it.analysis?.level })
        assertEquals(85.0, picks[3].maxPop, 0.0)
    }

    @Test
    fun bestOffsetPrefersAClearEveningOverHumidAndRainyOnes() {
        // Offset 0 is humid, offset 2 sits between two clear days, offset 3 runs into rain.
        val modes = listOf(
            Mode.HUMID, Mode.HUMID, Mode.CLEAR, Mode.CLEAR, Mode.RAIN, Mode.RAIN, Mode.RAIN, Mode.RAIN, Mode.RAIN,
        )
        val picks = evaluateWeek(week(modes), now, 21, 8, false)
        assertEquals(Level.MID, picks[0].analysis?.level)
        assertEquals(Level.BAD, picks[3].analysis?.level)
        assertEquals(2, bestOffset(picks))
    }

    @Test
    fun bestOffsetPicksTheEarlierEveningOnATie() {
        val picks = evaluateWeek(week(all(Mode.CLEAR, 9)), now, 21, 8, false)
        assertEquals(0, bestOffset(picks))
    }

    @Test
    fun recommendsNothingWhenEveryJudgedEveningIsBad() {
        val picks = evaluateWeek(week(all(Mode.RAIN, 9)), now, 21, 8, false)
        assertNull(bestOffset(picks))
    }

    @Test
    fun recommendsNothingWhenNoEveningCouldBeJudged() {
        assertNull(bestOffset(evaluateWeek(emptyList(), now, 21, 8, false)))
    }

    @Test
    fun aRoofCanTurnABadWeekIntoARecommendableOne() {
        val rows = week(all(Mode.RAIN, 9)).map { it.copy(pop = 80.0) }
        assertNull(bestOffset(evaluateWeek(rows, now, 21, 8, covered = false)))
        assertNotNull(bestOffset(evaluateWeek(rows, now, 21, 8, covered = true)))
    }
}
