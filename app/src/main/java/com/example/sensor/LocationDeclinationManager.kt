package com.example.sensor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.GeomagneticField
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Looper
import androidx.core.content.ContextCompat

/**
 * Manages device location checks and calculates true magnetic declination angle
 * using Android's built-in World Magnetic Model (GeomagneticField).
 */
object LocationDeclinationManager {

    /**
     * Checks if location permission (either FINE or COARSE) is granted.
     */
    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * Safely retrieves the best available last known location.
     */
    fun getLastKnownLocation(context: Context): Location? {
        if (!hasLocationPermission(context)) return null

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        return runCatching {
            val providers = lm.getProviders(true)
            var bestLocation: Location? = null
            for (provider in providers) {
                val loc = lm.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || loc.accuracy < bestLocation.accuracy || loc.time > bestLocation.time) {
                    bestLocation = loc
                }
            }
            bestLocation
        }.getOrNull()
    }

    /**
     * Requests location update to obtain a fresh location fix and deliver it via callback.
     * Checks last known location immediately and triggers a one-shot location request if needed.
     */
    fun requestLocation(context: Context, onLocationReceived: (Location) -> Unit) {
        if (!hasLocationPermission(context)) return

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return

        // 1. Immediately emit last known location if present
        val lastKnown = getLastKnownLocation(context)
        if (lastKnown != null) {
            onLocationReceived(lastKnown)
        }

        // 2. Request a fresh single-shot location update
        runCatching {
            val provider = when {
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                else -> LocationManager.PASSIVE_PROVIDER
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val signal = CancellationSignal()
                val executor = ContextCompat.getMainExecutor(context)
                lm.getCurrentLocation(provider, signal, executor) { location ->
                    if (location != null) {
                        onLocationReceived(location)
                    }
                }
            } else {
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        onLocationReceived(location)
                        lm.removeUpdates(this)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                if (provider == LocationManager.GPS_PROVIDER || provider == LocationManager.NETWORK_PROVIDER) {
                    lm.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                }
            }
        }
    }

    /**
     * Calculates magnetic declination angle in degrees for the given location and current time.
     * Positive is East, Negative is West.
     */
    fun calculateDeclination(location: Location): Float {
        return runCatching {
            val field = GeomagneticField(
                location.latitude.toFloat(),
                location.longitude.toFloat(),
                location.altitude.toFloat(),
                System.currentTimeMillis()
            )
            field.declination
        }.getOrDefault(0f)
    }
}
