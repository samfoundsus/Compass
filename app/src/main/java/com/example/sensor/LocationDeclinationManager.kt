package com.example.sensor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.GeomagneticField
import android.location.Location
import android.location.LocationManager
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
     * Safely retrieves the best available last known location without starting a heavy GPS loop.
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
