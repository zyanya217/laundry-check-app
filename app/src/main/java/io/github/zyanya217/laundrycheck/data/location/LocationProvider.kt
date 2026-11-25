package io.github.zyanya217.laundrycheck.data.location

data class Coordinates(val lat: Double, val lon: Double)

enum class LocationFailure { DENIED, UNAVAILABLE, TIMEOUT }

class LocationException(val failure: LocationFailure) : Exception(failure.name)

interface LocationProvider {
    /** One position fix. Throws [LocationException] when there is no permission, no fix, or it takes too long. */
    suspend fun current(): Coordinates
}
