package io.github.zyanya217.laundrycheck.widget

import io.github.zyanya217.laundrycheck.logic.CollectDay
import io.github.zyanya217.laundrycheck.logic.DRY_OK
import io.github.zyanya217.laundrycheck.logic.Level
import io.github.zyanya217.laundrycheck.logic.buildForecastContent
import io.github.zyanya217.laundrycheck.model.HourlyRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetDataTest {
    private val midnight = 480
    private val now = midnight + 13

    private fun clear(idx: Int) = HourlyRow(idx, temp = 22.0, rh = 60.0, wind = 3.0, pop = 3.0, code = 1)

    private fun ready(rows: List<HourlyRow>): WidgetData.Ready {
        val content = buildForecastContent(rows, emptyList(), now, 21, 8, covered = false)
        return buildWidgetData(content, "16:50") as WidgetData.Ready
    }

    @Test
    fun noForecastGivesNoData() {
        val content = buildForecastContent(emptyList(), emptyList(), now, 21, 8, covered = false)
        assertEquals(WidgetData.NoData, buildWidgetData(content, ""))
    }

    @Test
    fun summarisesTonightsWindow() {
        val d = ready(List(9 * 24) { clear(midnight + it) })
        assertEquals(Level.OK, d.level)
        assertEquals(21, d.startHour)
        assertEquals(8, d.collectHour)
        assertEquals(CollectDay.TOMORROW, d.collectDay)
        assertEquals(midnight / 24, d.dayNumber)
        assertEquals("16:50", d.updatedClock)
    }

    @Test
    fun showsAtMostThreeEveningsAndMarksOneBest() {
        val d = ready(List(9 * 24) { clear(midnight + it) })
        assertEquals(3, d.days.size)
        assertEquals(listOf(0, 1, 2), d.days.map { it.offset })
        assertEquals(1, d.days.count { it.best })
        assertTrue(d.days.first().best)
    }

    @Test
    fun rainChanceIsRoundedPercent() {
        val rainy = List(9 * 24) { i -> clear(midnight + i).copy(pop = 40.4) }
        assertEquals(40, ready(rainy).days.first().pop)
    }

    @Test
    fun meterFillStaysInsideTheTrack() {
        assertEquals(0.06f, meterFill(0.0), 1e-6f)
        assertEquals(1f, meterFill(DRY_OK * 5), 1e-6f)
        assertEquals(DRY_OK / (DRY_OK * 1.4), meterFill(DRY_OK).toDouble(), 1e-6)
    }
}
