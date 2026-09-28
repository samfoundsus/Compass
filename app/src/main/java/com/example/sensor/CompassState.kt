package com.example.sensor

enum class NorthReference {
    MAGNETIC,
    TRUE_NORTH
}

/**
 * State representing current heading, cardinal direction, sensor availability, and calibration metrics.
 *
 * @property heading Normalized azimuth angle in degrees [0f, 360f) reflecting active North Reference.
 * @property magneticHeading Raw smoothed magnetic heading angle [0f, 360f).
 * @property trueHeading True north heading angle [0f, 360f) with magnetic declination applied.
 * @property declination Active magnetic declination angle in degrees.
 * @property northReference Active North Reference mode (MAGNETIC or TRUE_NORTH).
 * @property direction 8-sector cardinal/intercardinal direction name (N, NE, E, SE, S, SW, W, NW).
 * @property isSensorAvailable True if the device possesses hardware sensors to compute orientation.
 * @property accuracy Sensor accuracy report (SensorManager.SENSOR_STATUS_*).
 * @property fieldStrengthMicroTesla Total magnetic field magnitude in microteslas (µT).
 */
data class CompassState(
    val heading: Float = 0f,
    val magneticHeading: Float = 0f,
    val trueHeading: Float = 0f,
    val declination: Float = 0f,
    val northReference: NorthReference = NorthReference.MAGNETIC,
    val direction: String = "N",
    val isSensorAvailable: Boolean = true,
    val accuracy: Int = 3,
    val fieldStrengthMicroTesla: Float = 45f
)

/**
 * Computes 8-way cardinal/intercardinal direction name from a normalized heading degree
 * with boundary hysteresis to prevent rapid flickering on sector edges.
 */
fun calculateDirectionSector(
    heading: Float,
    currentSector: String = "N",
    hysteresisDeg: Float = 1.0f
): String {
    val normalized = (heading % 360f + 360f) % 360f

    // Verify if heading is still safely within the current sector expanded by hysteresis
    if (isWithinSector(normalized, currentSector, hysteresisDeg)) {
        return currentSector
    }

    // Otherwise determine the new primary sector
    return when {
        normalized >= 337.5f || normalized < 22.5f -> "N"
        normalized < 67.5f -> "NE"
        normalized < 112.5f -> "E"
        normalized < 157.5f -> "SE"
        normalized < 202.5f -> "S"
        normalized < 247.5f -> "SW"
        normalized < 292.5f -> "W"
        else -> "NW"
    }
}

private fun isWithinSector(heading: Float, sector: String, margin: Float): Boolean {
    val (start, end) = when (sector) {
        "N" -> 337.5f - margin to 22.5f + margin
        "NE" -> 22.5f - margin to 67.5f + margin
        "E" -> 67.5f - margin to 112.5f + margin
        "SE" -> 112.5f - margin to 157.5f + margin
        "S" -> 157.5f - margin to 202.5f + margin
        "SW" -> 202.5f - margin to 247.5f + margin
        "W" -> 247.5f - margin to 292.5f + margin
        "NW" -> 292.5f - margin to 337.5f + margin
        else -> return false
    }

    return if (sector == "N") {
        heading >= start || heading < end
    } else {
        heading in start..end
    }
}
