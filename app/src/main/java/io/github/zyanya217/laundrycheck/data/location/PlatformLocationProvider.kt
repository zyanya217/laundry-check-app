package io.github.zyanya217.laundrycheck.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout

/**
 * Uses the platform [LocationManager] directly (minSdk 33, so no Play Services dependency is needed).
 * Coarse accuracy is enough: the result is only matched to the nearest district centre.
 */
class PlatformLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocationProvider {

    @SuppressLint("MissingPermission") // checked in hasPermission() before any location call
    override suspend fun current(): Coordinates {
        if (!hasPermission()) throw LocationException(LocationFailure.DENIED)
        val manager = context.getSystemService(LocationManager::class.java)
            ?: throw LocationException(LocationFailure.UNAVAILABLE)
        val provider = PROVIDERS.firstOrNull { manager.isProviderEnabled(it) }
            ?: throw LocationException(LocationFailure.UNAVAILABLE)

        try {
            // A fix from the last few minutes is plenty for choosing a district.
            manager.getLastKnownLocation(provider)
                ?.takeIf { System.currentTimeMillis() - it.time <= MAX_AGE_MS }
                ?.let { return it.toCoordinates() }

            val fix = withTimeout(TIMEOUT_MS) { awaitCurrent(manager, provider) }
                ?: throw LocationException(LocationFailure.UNAVAILABLE)
            return fix.toCoordinates()
        } catch (_: TimeoutCancellationException) {
            throw LocationException(LocationFailure.TIMEOUT)
        } catch (_: SecurityException) {
            throw LocationException(LocationFailure.DENIED)
        }
    }

    private fun hasPermission(): Boolean =
        listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION).any {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    @SuppressLint("MissingPermission")
    private suspend fun awaitCurrent(manager: LocationManager, provider: String): Location? =
        suspendCancellableCoroutine { continuation ->
            val signal = CancellationSignal()
            continuation.invokeOnCancellation { signal.cancel() }
            manager.getCurrentLocation(provider, signal, ContextCompat.getMainExecutor(context)) { location ->
                continuation.resume(location)
            }
        }

    private fun Location.toCoordinates() = Coordinates(latitude, longitude)

    private companion object {
        val PROVIDERS = listOf(LocationManager.FUSED_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
        const val TIMEOUT_MS = 10_000L
        const val MAX_AGE_MS = 5 * 60 * 1000L
    }
}
