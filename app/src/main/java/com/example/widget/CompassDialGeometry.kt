package com.example.widget

import android.graphics.Path
import android.graphics.PointF
import com.example.ui.compass.CompassDialShape
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

enum class MarkerCategory {
    CARDINAL,   // N, E, S, W
    DIAGONAL    // NE, SE, SW, NW
}

data class DirectionMarkerInfo(
    val label: String,
    val angleDegrees: Float,
    val category: MarkerCategory
)

/**
 * Shared geometry and layout calculations for compass dial shapes and directional markers.
 * Used by both Jetpack Compose in-app UI and Native AppWidget Canvas rendering.
 */
object CompassDialGeometry {

    val DIRECTION_MARKERS = listOf(
        DirectionMarkerInfo("N", 0f, MarkerCategory.CARDINAL),
        DirectionMarkerInfo("NE", 45f, MarkerCategory.DIAGONAL),
        DirectionMarkerInfo("E", 90f, MarkerCategory.CARDINAL),
        DirectionMarkerInfo("SE", 135f, MarkerCategory.DIAGONAL),
        DirectionMarkerInfo("S", 180f, MarkerCategory.CARDINAL),
        DirectionMarkerInfo("SW", 225f, MarkerCategory.DIAGONAL),
        DirectionMarkerInfo("W", 270f, MarkerCategory.CARDINAL),
        DirectionMarkerInfo("NW", 315f, MarkerCategory.DIAGONAL)
    )

    fun createPathForShape(
        shape: CompassDialShape,
        width: Float,
        height: Float
    ): Path {
        return when (shape) {
            is CompassDialShape.Sunny -> createSunnyPath(width, height)
            is CompassDialShape.Circle -> createCirclePath(width, height)
            is CompassDialShape.Diamond -> createDiamondPath(width, height)
            is CompassDialShape.Octagon -> createOctagonPath(width, height)
        }
    }

    fun createSunnyPath(
        width: Float,
        height: Float,
        lobes: Int = 8,
        depthRatio: Float = 0.12f
    ): Path {
        val path = Path()
        val cx = width / 2f
        val cy = height / 2f
        val maxRadius = min(width, height) / 2f
        val minRadius = maxRadius * (1f - depthRatio)
        val amp = (maxRadius - minRadius) / 2f
        val midRadius = (maxRadius + minRadius) / 2f

        val totalPoints = lobes * 32
        for (i in 0..totalPoints) {
            val progress = i.toFloat() / totalPoints
            val angleRad = progress * (2f * Math.PI.toFloat())
            val lobeAngle = lobes * angleRad
            val r = midRadius + amp * cos(lobeAngle)
            val x = cx + r * sin(angleRad)
            val y = cy - r * cos(angleRad)

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        return path
    }

    fun createDiamondPath(
        width: Float,
        height: Float,
        depthRatio: Float = 0.18f
    ): Path {
        val path = Path()
        val cx = width / 2f
        val cy = height / 2f
        val maxRadius = min(width, height) / 2f
        val minRadius = maxRadius * (1f - depthRatio)
        val amp = (maxRadius - minRadius) / 2f
        val midRadius = (maxRadius + minRadius) / 2f

        val totalPoints = 256
        for (i in 0..totalPoints) {
            val progress = i.toFloat() / totalPoints
            val angleRad = progress * (2f * Math.PI.toFloat())
            val lobeAngle = 4 * angleRad
            val r = midRadius + amp * cos(lobeAngle)
            val x = cx + r * sin(angleRad)
            val y = cy - r * cos(angleRad)

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        return path
    }

    fun createOctagonPath(
        width: Float,
        height: Float,
        cornerRoundingFraction: Float = 0.45f
    ): Path {
        val path = Path()
        val cx = width / 2f
        val cy = height / 2f
        val maxRadius = min(width, height) / 2f

        val cornerAnglesDeg = doubleArrayOf(22.5, 67.5, 112.5, 157.5, 202.5, 247.5, 292.5, 337.5)
        val sideAnglesDeg = doubleArrayOf(0.0, 45.0, 90.0, 135.0, 180.0, 225.0, 270.0, 315.0)
        val sideRadius = maxRadius * cos(Math.toRadians(22.5)).toFloat()

        val vertices = cornerAnglesDeg.map { deg ->
            val rad = Math.toRadians(deg)
            PointF(
                (cx + maxRadius * sin(rad)).toFloat(),
                (cy - maxRadius * cos(rad)).toFloat()
            )
        }

        val midpoints = sideAnglesDeg.map { deg ->
            val rad = Math.toRadians(deg)
            PointF(
                (cx + sideRadius * sin(rad)).toFloat(),
                (cy - sideRadius * cos(rad)).toFloat()
            )
        }

        for (i in 0 until 8) {
            val prevSideIdx = i
            val nextSideIdx = (i + 1) % 8
            val v = vertices[i]
            val mPrev = midpoints[prevSideIdx]
            val mNext = midpoints[nextSideIdx]

            val pIn = PointF(
                mPrev.x + (v.x - mPrev.x) * (1f - cornerRoundingFraction),
                mPrev.y + (v.y - mPrev.y) * (1f - cornerRoundingFraction)
            )
            val pOut = PointF(
                mNext.x + (v.x - mNext.x) * (1f - cornerRoundingFraction),
                mNext.y + (v.y - mNext.y) * (1f - cornerRoundingFraction)
            )

            val cp1 = PointF(
                pIn.x + (v.x - pIn.x) * 0.60f,
                pIn.y + (v.y - pIn.y) * 0.60f
            )
            val cp2 = PointF(
                pOut.x + (v.x - pOut.x) * 0.60f,
                pOut.y + (v.y - pOut.y) * 0.60f
            )

            if (i == 0) {
                path.moveTo(pIn.x, pIn.y)
            } else {
                path.lineTo(pIn.x, pIn.y)
            }

            path.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, pOut.x, pOut.y)
        }

        path.close()
        return path
    }

    fun createCirclePath(
        width: Float,
        height: Float
    ): Path {
        val path = Path()
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width, height) / 2f
        path.addCircle(cx, cy, radius, Path.Direction.CW)
        return path
    }

    fun getRadialDistance(shape: CompassDialShape, radius: Float, category: MarkerCategory): Float {
        return when (shape) {
            is CompassDialShape.Diamond -> {
                when (category) {
                    MarkerCategory.CARDINAL -> radius * 0.77f
                    MarkerCategory.DIAGONAL -> radius * 0.59f
                }
            }
            is CompassDialShape.Octagon -> radius * 0.72f
            else -> radius * 0.77f
        }
    }
}
