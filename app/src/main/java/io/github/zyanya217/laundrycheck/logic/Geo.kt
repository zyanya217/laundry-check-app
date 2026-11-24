package io.github.zyanya217.laundrycheck.logic

import io.github.zyanya217.laundrycheck.model.DistrictEntry
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** A position farther than this from every district centre is treated as outside the covered area. */
const val MAX_GPS_KM = 60.0

data class NearestDistrict(val county: String, val district: String, val km: Double)

fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    fun rad(d: Double) = d * Math.PI / 180
    val a = sin(rad(lat2 - lat1) / 2).pow(2) +
        cos(rad(lat1)) * cos(rad(lat2)) * sin(rad(lon2 - lon1) / 2).pow(2)
    return 6371 * 2 * asin(sqrt(a))
}

/**
 * Nearest district centre, or null when the table has no coordinates at all.
 * On an exact tie the first county/district in iteration order wins, so pass an ordered map.
 */
fun nearestDistrict(lat: Double, lon: Double, table: Map<String, List<DistrictEntry>>): NearestDistrict? {
    var best: NearestDistrict? = null
    for ((county, districts) in table) {
        for (d in districts) {
            val dLat = d.lat ?: continue
            val dLon = d.lon ?: continue
            val km = haversineKm(lat, lon, dLat, dLon)
            if (best == null || km < best.km) best = NearestDistrict(county, d.name, km)
        }
    }
    return best
}
