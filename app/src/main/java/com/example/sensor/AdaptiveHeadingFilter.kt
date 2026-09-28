package com.example.sensor

import kotlin.math.abs

enum class SmoothingMode {
    ADAPTIVE,
    HIGH_STABILITY,
    RESPONSIVE
}

/**
 * Adaptive smoothing filter for compass heading.
 * Handles circular 0°/360° boundary smoothly and dynamically adjusts response rate
 * based on angular displacement:
 * - Small micro-movements (< 1.5°): Stronger filtering to completely eliminate sensor jitter.
 * - Intentional movements (1.5° - 20°): Continuous progressive interpolation.
 * - Rapid turns (> 20°): Fast snappy tracking with near-zero latency.
 *
 * Supports configurable smoothing modes (Adaptive, High Stability, Responsive)
 * while keeping 0°/360° wrap-around calculations pristine.
 */
class AdaptiveHeadingFilter(
    private var mode: SmoothingMode = SmoothingMode.ADAPTIVE
) {
    private var smoothedHeading: Float = 0f
    private var isInitialized: Boolean = false

    private val minAlpha: Float
        get() = when (mode) {
            SmoothingMode.ADAPTIVE -> 0.15f
            SmoothingMode.HIGH_STABILITY -> 0.05f
            SmoothingMode.RESPONSIVE -> 0.45f
        }

    private val maxAlpha: Float
        get() = when (mode) {
            SmoothingMode.ADAPTIVE -> 0.85f
            SmoothingMode.HIGH_STABILITY -> 0.45f
            SmoothingMode.RESPONSIVE -> 0.95f
        }

    fun setSmoothingMode(newMode: SmoothingMode) {
        this.mode = newMode
    }

    fun reset() {
        isInitialized = false
    }

    /**
     * Filters the raw sensor heading degree and returns the smoothed, normalized heading in [0, 360).
     */
    fun filter(rawHeading: Float): Float {
        if (!isInitialized) {
            smoothedHeading = (rawHeading % 360f + 360f) % 360f
            isInitialized = true
            return smoothedHeading
        }

        // Calculate shortest angular displacement across 0°/360° circle boundary
        // ((target - current + 540) % 360) - 180 results in a delta in [-180, 180]
        val diff = (rawHeading - smoothedHeading + 540f) % 360f - 180f
        val absDiff = abs(diff)

        // Dynamic alpha based on angular displacement and active smoothing mode
        val adaptiveAlpha = when {
            absDiff < 0.2f -> minAlpha * 0.5f // Extremely subtle noise filtered
            absDiff < 20.0f -> {
                val progress = (absDiff - 0.2f) / 19.8f
                minAlpha + progress * (maxAlpha - minAlpha)
            }
            else -> maxAlpha
        }

        smoothedHeading = (smoothedHeading + adaptiveAlpha * diff + 360f) % 360f
        return smoothedHeading
    }
}
