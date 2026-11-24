package io.github.zyanya217.laundrycheck.model

/** A township/district from `districts.json`. Coordinates are the district centre, used for GPS lookup. */
data class DistrictEntry(
    val name: String,
    val geocode: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
)
