package com.example.sensor

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.math.abs

/**
 * Handles haptic tick feedback when the heading crosses cardinal directions (N, E, S, W).
 * Implements a hysteresis deadband lock to prevent repeated vibrations from sensor noise.
 */
class HapticFeedbackHelper(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var activeCardinalZone: Int? = null // 0 for N, 90 for E, 180 for S, 270 for W

    private val cardinalPoints = listOf(0, 90, 180, 270)

    /**
     * Checks if the heading is crossing a cardinal point and triggers haptic feedback if enabled.
     */
    fun onHeadingChanged(heading: Float, enabled: Boolean) {
        if (!enabled || vibrator == null || !vibrator.hasVibrator()) {
            return
        }

        val normHeading = (heading % 360f + 360f) % 360f

        // Check if currently inside the entry window (±1.5°) of any cardinal point
        var matchedPoint: Int? = null
        for (point in cardinalPoints) {
            val diff = abs((normHeading - point + 540f) % 360f - 180f)
            if (diff <= 1.5f) {
                matchedPoint = point
                break
            }
        }

        if (matchedPoint != null) {
            // If entering a new cardinal zone that wasn't already active
            if (activeCardinalZone != matchedPoint) {
                activeCardinalZone = matchedPoint
                performHapticTick()
            }
        } else {
            // Check if we exited the deadband (outside ±4.0°) of the active zone to re-arm
            activeCardinalZone?.let { zone ->
                val diff = abs((normHeading - zone + 540f) % 360f - 180f)
                if (diff > 4.0f) {
                    activeCardinalZone = null
                }
            }
        }
    }

    private fun performHapticTick() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            }
        }
    }
}
