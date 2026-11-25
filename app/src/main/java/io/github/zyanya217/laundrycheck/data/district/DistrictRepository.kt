package io.github.zyanya217.laundrycheck.data.district

import io.github.zyanya217.laundrycheck.model.DistrictEntry

interface DistrictRepository {
    /** Every county with its districts, in the order of `districts.json`. Loaded once, then cached. */
    suspend fun load(): Map<String, List<DistrictEntry>>
}
