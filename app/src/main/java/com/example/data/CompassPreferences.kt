package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.sensor.LocationDeclinationManager
import com.example.sensor.NorthReference
import com.example.sensor.SmoothingMode
import com.example.ui.compass.CompassDialSettings
import com.example.ui.compass.CompassDialShape
import com.example.ui.theme.AppThemeState
import com.example.ui.theme.ThemeMode

/**
 * Persistence layer for appearance, compass reference, smoothing, haptics, and dial preferences
 * using SharedPreferences with observable Compose states for immediate UI reactivity.
 */
object CompassPreferences {
    private const val PREFS_NAME = "compass_preferences"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_DYNAMIC_COLOR = "dynamic_color"
    private const val KEY_DIAL_SHAPE = "dial_shape"
    private const val KEY_NORTH_REF = "north_reference"
    private const val KEY_SMOOTHING_MODE = "smoothing_mode"
    private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
    private const val KEY_SAVED_DECLINATION = "saved_declination"

    private var prefs: SharedPreferences? = null

    // Observable states
    var northReference by mutableStateOf(NorthReference.MAGNETIC)
    var smoothingMode by mutableStateOf(SmoothingMode.ADAPTIVE)
    var hapticsEnabled by mutableStateOf(true)
    var declination by mutableFloatStateOf(0f)
    var declinationStatus by mutableStateOf("Location required")

    fun init(context: Context) {
        val sp = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = sp

        // 1. Theme Mode
        val savedTheme = sp.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        AppThemeState.themeMode = runCatching {
            ThemeMode.valueOf(savedTheme ?: ThemeMode.SYSTEM.name)
        }.getOrDefault(ThemeMode.SYSTEM)

        // 2. Dynamic Color (defaults to true for Material You)
        AppThemeState.dynamicColorEnabled = sp.getBoolean(KEY_DYNAMIC_COLOR, true)

        // 3. Dial Shape (defaults to Sunny)
        val savedShapeName = sp.getString(KEY_DIAL_SHAPE, "Sunny")
        CompassDialSettings.currentShape = when (savedShapeName) {
            "Circle" -> CompassDialShape.Circle
            "Diamond" -> CompassDialShape.Diamond
            "Octagon" -> CompassDialShape.Octagon
            else -> CompassDialShape.Sunny
        }

        // 4. North Reference (defaults to MAGNETIC)
        val savedNorthRef = sp.getString(KEY_NORTH_REF, NorthReference.MAGNETIC.name)
        val loadedRef = runCatching {
            NorthReference.valueOf(savedNorthRef ?: NorthReference.MAGNETIC.name)
        }.getOrDefault(NorthReference.MAGNETIC)

        // If True North was saved but permission is not granted, fall back to Magnetic
        if (loadedRef == NorthReference.TRUE_NORTH && !LocationDeclinationManager.hasLocationPermission(context)) {
            northReference = NorthReference.MAGNETIC
            sp.edit().putString(KEY_NORTH_REF, NorthReference.MAGNETIC.name).apply()
        } else {
            northReference = loadedRef
        }

        // 5. Smoothing Mode (defaults to ADAPTIVE)
        val savedSmoothing = sp.getString(KEY_SMOOTHING_MODE, SmoothingMode.ADAPTIVE.name)
        smoothingMode = runCatching {
            SmoothingMode.valueOf(savedSmoothing ?: SmoothingMode.ADAPTIVE.name)
        }.getOrDefault(SmoothingMode.ADAPTIVE)

        // 6. Haptic Feedback (defaults to true)
        hapticsEnabled = sp.getBoolean(KEY_HAPTICS_ENABLED, true)

        // 7. Declination calculation if location is available
        declination = sp.getFloat(KEY_SAVED_DECLINATION, 0f)
        updateDeclinationFromLocation(context)
    }

    fun setThemeMode(mode: ThemeMode) {
        AppThemeState.themeMode = mode
        prefs?.edit()?.putString(KEY_THEME_MODE, mode.name)?.apply()
    }

    fun setDynamicColor(enabled: Boolean) {
        AppThemeState.dynamicColorEnabled = enabled
        prefs?.edit()?.putBoolean(KEY_DYNAMIC_COLOR, enabled)?.apply()
    }

    fun setDialShape(shape: CompassDialShape) {
        CompassDialSettings.currentShape = shape
        val shapeName = when (shape) {
            CompassDialShape.Sunny -> "Sunny"
            CompassDialShape.Circle -> "Circle"
            CompassDialShape.Diamond -> "Diamond"
            CompassDialShape.Octagon -> "Octagon"
        }
        prefs?.edit()?.putString(KEY_DIAL_SHAPE, shapeName)?.apply()
    }

    fun setNorthReferenceMode(ref: NorthReference, context: Context? = null) {
        val targetRef = if (ref == NorthReference.TRUE_NORTH && context != null && !LocationDeclinationManager.hasLocationPermission(context)) {
            NorthReference.MAGNETIC
        } else {
            ref
        }
        northReference = targetRef
        prefs?.edit()?.putString(KEY_NORTH_REF, targetRef.name)?.apply()
        if (context != null) {
            updateDeclinationFromLocation(context)
        }
    }

    fun setSmoothing(mode: SmoothingMode) {
        smoothingMode = mode
        prefs?.edit()?.putString(KEY_SMOOTHING_MODE, mode.name)?.apply()
    }

    fun setHaptics(enabled: Boolean) {
        hapticsEnabled = enabled
        prefs?.edit()?.putBoolean(KEY_HAPTICS_ENABLED, enabled)?.apply()
    }

    fun checkAndEnforcePermissionState(context: Context) {
        if (!LocationDeclinationManager.hasLocationPermission(context)) {
            if (northReference == NorthReference.TRUE_NORTH) {
                setNorthReferenceMode(NorthReference.MAGNETIC, context)
            }
            declinationStatus = if (declination != 0f) {
                formatDeclination(declination) + " (Cached)"
            } else {
                "Location required"
            }
        } else {
            updateDeclinationFromLocation(context)
        }
    }

    fun updateDeclinationFromLocation(context: Context) {
        if (!LocationDeclinationManager.hasLocationPermission(context)) {
            declinationStatus = if (declination != 0f) {
                formatDeclination(declination) + " (Cached)"
            } else {
                "Location required"
            }
            return
        }

        val location = LocationDeclinationManager.getLastKnownLocation(context)
        if (location != null) {
            val computedDeclination = LocationDeclinationManager.calculateDeclination(location)
            declination = computedDeclination
            prefs?.edit()?.putFloat(KEY_SAVED_DECLINATION, computedDeclination)?.apply()
            declinationStatus = formatDeclination(computedDeclination)
        } else {
            declinationStatus = if (declination != 0f) {
                formatDeclination(declination) + " (Cached)"
            } else {
                "Location unavailable"
            }
        }
    }

    private fun formatDeclination(dec: Float): String {
        val sign = if (dec >= 0) "+" else ""
        val direction = if (dec >= 0) "E" else "W"
        return String.format("%s%.1f° %s", sign, kotlin.math.abs(dec), direction)
    }
}
