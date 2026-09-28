package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import com.example.data.CompassPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Manages device hardware sensors (Rotation Vector with Accelerometer + Magnetometer fallback)
 * to stream orientation, heading in degrees [0, 360), and 8-way compass sectors.
 * Integrates North Reference (Magnetic vs True), location-based declination, smoothing modes,
 * and cardinal haptic feedback.
 */
class OrientationSensorManager(private val context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val hapticFeedbackHelper = HapticFeedbackHelper(context)

    /**
     * Checks if the device has either Rotation Vector sensor or Accelerometer + Magnetometer.
     */
    fun hasOrientationSensors(): Boolean {
        val sm = sensorManager ?: return false
        val rotationVectorSensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationVectorSensor != null) return true

        val accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        return accelerometer != null && magnetometer != null
    }

    /**
     * Streams real-time compass orientation updates in a lifecycle-safe Coroutines Flow.
     * Starts listening when collected and unregisters listener immediately upon completion.
     */
    fun getOrientationFlow(): Flow<CompassState> = callbackFlow {
        val sm = sensorManager
        if (sm == null || !hasOrientationSensors()) {
            trySend(CompassState(isSensorAvailable = false))
            awaitClose { }
            return@callbackFlow
        }

        val rotationVectorSensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelerometerSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometerSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val rotationMatrix = FloatArray(9)
        val remappedMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)

        val gravityValues = FloatArray(3)
        val geomagneticValues = FloatArray(3)
        var hasGravity = false
        var hasGeomagnetic = false

        val headingFilter = AdaptiveHeadingFilter(CompassPreferences.smoothingMode)
        var currentDirection = "N"
        var lastEmittedHeading = -1f
        var currentAccuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH
        var magneticFieldStrength = 45f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                var rawHeading: Float? = null

                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    currentAccuracy = event.accuracy
                    runCatching {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        remapForDisplayRotation(rotationMatrix, remappedMatrix)
                        SensorManager.getOrientation(remappedMatrix, orientationAngles)
                        val rawAzimuthDeg = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                        if (rawAzimuthDeg.isFinite() && !rawAzimuthDeg.isNaN()) {
                            rawHeading = (rawAzimuthDeg + 360f) % 360f
                        }
                    }
                } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, gravityValues, 0, minOf(event.values.size, 3))
                    hasGravity = true
                    if (hasGeomagnetic) {
                        rawHeading = computeHeadingFromGravityAndGeomagnetic()
                    }
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    currentAccuracy = event.accuracy
                    System.arraycopy(event.values, 0, geomagneticValues, 0, minOf(event.values.size, 3))
                    hasGeomagnetic = true

                    // Calculate total magnetic field magnitude
                    val magX = event.values[0]
                    val magY = event.values[1]
                    val magZ = event.values[2]
                    magneticFieldStrength = sqrt(magX * magX + magY * magY + magZ * magZ)

                    if (hasGravity) {
                        rawHeading = computeHeadingFromGravityAndGeomagnetic()
                    }
                }

                rawHeading?.let { headingValue ->
                    // Keep smoothing mode in sync with preferences
                    headingFilter.setSmoothingMode(CompassPreferences.smoothingMode)

                    val smoothedMagnetic = headingFilter.filter(headingValue)
                    val activeNorthRef = CompassPreferences.northReference
                    val declination = CompassPreferences.declination

                    val trueHeading = (smoothedMagnetic + declination + 360f) % 360f
                    val effectiveHeading = if (activeNorthRef == NorthReference.TRUE_NORTH) {
                        trueHeading
                    } else {
                        smoothedMagnetic
                    }

                    currentDirection = calculateDirectionSector(effectiveHeading, currentDirection)

                    // Trigger haptic feedback when crossing cardinal directions
                    hapticFeedbackHelper.onHeadingChanged(effectiveHeading, CompassPreferences.hapticsEnabled)

                    // Optimize emission: publish when heading difference >= 0.05° or sector changes
                    val deltaSinceLast = abs((effectiveHeading - lastEmittedHeading + 540f) % 360f - 180f)
                    if (lastEmittedHeading < 0f || deltaSinceLast >= 0.05f) {
                        lastEmittedHeading = effectiveHeading
                        trySend(
                            CompassState(
                                heading = effectiveHeading,
                                magneticHeading = smoothedMagnetic,
                                trueHeading = trueHeading,
                                declination = declination,
                                northReference = activeNorthRef,
                                direction = currentDirection,
                                isSensorAvailable = true,
                                accuracy = currentAccuracy,
                                fieldStrengthMicroTesla = magneticFieldStrength
                            )
                        )
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                currentAccuracy = accuracy
            }

            private fun computeHeadingFromGravityAndGeomagnetic(): Float? {
                return runCatching {
                    val r = FloatArray(9)
                    val i = FloatArray(9)
                    if (SensorManager.getRotationMatrix(r, i, gravityValues, geomagneticValues)) {
                        remapForDisplayRotation(r, remappedMatrix)
                        SensorManager.getOrientation(remappedMatrix, orientationAngles)
                        val rawAzimuthDeg = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                        if (rawAzimuthDeg.isFinite() && !rawAzimuthDeg.isNaN()) {
                            (rawAzimuthDeg + 360f) % 360f
                        } else null
                    } else null
                }.getOrNull()
            }
        }

        var registeredAny = false
        if (rotationVectorSensor != null) {
            registeredAny = sm.registerListener(listener, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME)
        }

        // Always register magnetic field sensor if available to monitor field strength & calibration accuracy
        if (magnetometerSensor != null && rotationVectorSensor != null) {
            sm.registerListener(listener, magnetometerSensor, SensorManager.SENSOR_DELAY_UI)
        }

        if (!registeredAny) {
            val regAccel = if (accelerometerSensor != null) {
                sm.registerListener(listener, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)
            } else false
            val regMag = if (magnetometerSensor != null) {
                sm.registerListener(listener, magnetometerSensor, SensorManager.SENSOR_DELAY_GAME)
            } else false
            registeredAny = regAccel && regMag
        }

        if (!registeredAny) {
            trySend(CompassState(isSensorAvailable = false))
        }

        awaitClose {
            sm.unregisterListener(listener)
        }
    }

    private fun getDisplayRotation(): Int {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
                displayManager?.getDisplay(Display.DEFAULT_DISPLAY)?.rotation
            } else {
                @Suppress("DEPRECATION")
                (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay?.rotation
            }
        }.getOrNull() ?: Surface.ROTATION_0
    }

    private fun remapForDisplayRotation(inMatrix: FloatArray, outMatrix: FloatArray) {
        val displayRotation = getDisplayRotation()

        when (displayRotation) {
            Surface.ROTATION_0 -> SensorManager.remapCoordinateSystem(
                inMatrix,
                SensorManager.AXIS_X,
                SensorManager.AXIS_Y,
                outMatrix
            )
            Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(
                inMatrix,
                SensorManager.AXIS_Y,
                SensorManager.AXIS_MINUS_X,
                outMatrix
            )
            Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(
                inMatrix,
                SensorManager.AXIS_MINUS_X,
                SensorManager.AXIS_MINUS_Y,
                outMatrix
            )
            Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(
                inMatrix,
                SensorManager.AXIS_MINUS_Y,
                SensorManager.AXIS_X,
                outMatrix
            )
            else -> SensorManager.remapCoordinateSystem(
                inMatrix,
                SensorManager.AXIS_X,
                SensorManager.AXIS_Y,
                outMatrix
            )
        }
    }
}
