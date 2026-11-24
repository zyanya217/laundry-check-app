package io.github.zyanya217.laundrycheck.logic

import io.github.zyanya217.laundrycheck.model.DistrictEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoTest {
    private val table = linkedMapOf(
        "甲市" to listOf(
            DistrictEntry("A區", lat = 25.0, lon = 121.5),
            DistrictEntry("B區", lat = 25.1, lon = 121.6),
            DistrictEntry("無座標區"),
        ),
        "乙縣" to listOf(DistrictEntry("C鄉", lat = 23.0, lon = 120.2)),
    )

    @Test
    fun measuresADegreeOfLatitudeAsAbout111Km() {
        val km = haversineKm(25.0, 121.0, 26.0, 121.0)
        assertTrue(km > 110)
        assertTrue(km < 112)
    }

    @Test
    fun picksTheNearestDistrictAcrossCountiesAndSkipsEntriesWithoutCoordinates() {
        val a = nearestDistrict(25.09, 121.59, table)
        assertEquals("甲市", a?.county)
        assertEquals("B區", a?.district)
        val b = nearestDistrict(23.01, 120.21, table)
        assertEquals("乙縣", b?.county)
        assertEquals("C鄉", b?.district)
    }

    @Test
    fun returnsNullWhenNoDistrictHasCoordinates() {
        assertNull(nearestDistrict(25.0, 121.0, mapOf("甲市" to listOf(DistrictEntry("A區")))))
    }
}
