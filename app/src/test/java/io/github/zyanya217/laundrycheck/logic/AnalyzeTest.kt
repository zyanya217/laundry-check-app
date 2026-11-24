package io.github.zyanya217.laundrycheck.logic

import io.github.zyanya217.laundrycheck.model.HourlyRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyzeTest {
    // idx 480 is a Taipei midnight, so 480 + 21 is 21:00.
    private fun night(n: Int, over: (Int) -> HourlyRow.() -> HourlyRow = { { this } }): List<HourlyRow> =
        List(n) { i ->
            HourlyRow(idx = 480 + 21 + i, temp = 21.0, rh = 62.0, wind = 2.8, pop = 3.0, code = 1).let(over(i))
        }

    @Test
    fun clearBreezyNightIsGoodToHangOutsideAndDriesByMorning() {
        val a = analyze(night(12), covered = false)
        assertEquals(Level.OK, a.level)
        assertEquals(Dryness.DRY, a.dry)
        assertEquals(Reason.OK_DRY, a.reason)
    }

    @Test
    fun coolHumidAirWithoutRainIsBorderlineAndLeavesClothesWet() {
        val a = analyze(night(12) { { copy(temp = 17.0, rh = 95.0, wind = 1.0, pop = 14.0, code = 7) } }, covered = false)
        assertEquals(Level.MID, a.level)
        assertEquals(Reason.HUMID, a.reason)
        assertEquals(Dryness.WET, a.dry)
    }

    private fun downpour(): List<HourlyRow> =
        night(12) { i -> { if (i in 3..6) copy(pop = 80.0, rh = 95.0) else this } }

    @Test
    fun likelyDownpourMeansDryIndoorsAndReportsWhenItRains() {
        val a = analyze(downpour(), covered = false)
        assertEquals(Level.BAD, a.level)
        assertEquals(Reason.RAIN_BAD, a.reason)
        assertEquals(0, a.rainFrom)
        assertEquals(4, a.rainTo)
        assertEquals(80.0, a.rainPop, 0.0)
    }

    @Test
    fun roofSoftensTheSameForecastToBorderline() {
        val rows = downpour()
        assertEquals(Level.MID, analyze(rows, covered = true).level)
        assertEquals(Reason.RAIN_MID, analyze(rows, covered = true).reason)
    }

    @Test
    fun windAndWarmthRaiseTheDrynessScore() {
        val slow = analyze(night(12) { { copy(wind = 0.5) } }, covered = false).score
        val fast = analyze(night(12) { { copy(wind = 4.0) } }, covered = false).score
        assertTrue(fast > slow)
    }
}
