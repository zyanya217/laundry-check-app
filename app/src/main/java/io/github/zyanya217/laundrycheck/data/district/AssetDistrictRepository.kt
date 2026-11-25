package io.github.zyanya217.laundrycheck.data.district

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.zyanya217.laundrycheck.model.DistrictEntry
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Reads `assets/districts.json`, the same file the web app generates with `npm run gen:districts`. */
@Singleton
class AssetDistrictRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) : DistrictRepository {

    @Serializable
    private data class DistrictDto(
        val name: String,
        val geocode: String? = null,
        val lat: Double? = null,
        val lon: Double? = null,
    )

    @Volatile
    private var cached: Map<String, List<DistrictEntry>>? = null

    override suspend fun load(): Map<String, List<DistrictEntry>> =
        cached ?: withContext(Dispatchers.IO) {
            val text = context.assets.open(FILE).bufferedReader().use { it.readText() }
            // Json decodes a map into a LinkedHashMap, so the file's county order is kept.
            json.decodeFromString<Map<String, List<DistrictDto>>>(text)
                .mapValues { (_, list) -> list.map { DistrictEntry(it.name, it.geocode, it.lat, it.lon) } }
                .also { cached = it }
        }

    private companion object {
        const val FILE = "districts.json"
    }
}
