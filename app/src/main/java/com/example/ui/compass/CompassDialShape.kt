package com.example.ui.compass

import androidx.annotation.StringRes
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.example.R
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * An organic rounded starburst-like Material 3 shape.
 * Features smooth, soft-scalloped lobes with zero sharp corners.
 */
class SunnyShape(
    val lobes: Int = 8,
    val depthRatio: Float = 0.12f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxRadius = min(size.width, size.height) / 2f
        val minRadius = maxRadius * (1f - depthRatio)
        val amp = (maxRadius - minRadius) / 2f
        val midRadius = (maxRadius + minRadius) / 2f

        val totalPoints = lobes * 32 // 256 points for silky smooth curve
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
        return Outline.Generic(path)
    }
}

/**
 * A soft, organic Material You diamond shape.
 * Features 4 smooth, continuous rounded extremities pointing toward N, E, S, W
 * with gently convex contours and zero sharp vertices.
 */
class DiamondShape(
    val depthRatio: Float = 0.18f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxRadius = min(size.width, size.height) / 2f
        val minRadius = maxRadius * (1f - depthRatio)
        val amp = (maxRadius - minRadius) / 2f
        val midRadius = (maxRadius + minRadius) / 2f

        val totalPoints = 256 // High-precision sampling for silky smooth continuous Bézier-like curvature
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
        return Outline.Generic(path)
    }
}

/**
 * A soft, organic Material You Octagon shape.
 * Features 8 recognizable sides with prominently rounded corners and smooth transitions,
 * avoiding rigid polygon lines and sharp vertices.
 */
class OctagonShape(
    val cornerRoundingFraction: Float = 0.45f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxRadius = min(size.width, size.height) / 2f

        // 8 corner vertices at angles 22.5°, 67.5°, 112.5°, 157.5°, 202.5°, 247.5°, 292.5°, 337.5°
        val cornerAnglesDeg = doubleArrayOf(22.5, 67.5, 112.5, 157.5, 202.5, 247.5, 292.5, 337.5)
        // 8 side midpoints at angles 0° (N), 45° (NE), 90° (E), 135° (SE), 180° (S), 225° (SW), 270° (W), 315° (NW)
        val sideAnglesDeg = doubleArrayOf(0.0, 45.0, 90.0, 135.0, 180.0, 225.0, 270.0, 315.0)

        val sideRadius = maxRadius * cos(Math.toRadians(22.5)).toFloat()

        data class PointF(val x: Float, val y: Float)

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

        // Generate smooth cubic curves around all 8 corners with soft Material You rounding
        for (i in 0 until 8) {
            val prevSideIdx = i
            val nextSideIdx = (i + 1) % 8
            val v = vertices[i]
            val mPrev = midpoints[prevSideIdx]
            val mNext = midpoints[nextSideIdx]

            // Point on edge entering the corner
            val pIn = PointF(
                mPrev.x + (v.x - mPrev.x) * (1f - cornerRoundingFraction),
                mPrev.y + (v.y - mPrev.y) * (1f - cornerRoundingFraction)
            )
            // Point on edge exiting the corner
            val pOut = PointF(
                mNext.x + (v.x - mNext.x) * (1f - cornerRoundingFraction),
                mNext.y + (v.y - mNext.y) * (1f - cornerRoundingFraction)
            )

            // Cubic control points for smooth rounded transition
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
        return Outline.Generic(path)
    }
}

/**
 * Dial shape definition for current and future phases.
 */
sealed interface CompassDialShape {
    val shape: Shape
    @get:StringRes
    val nameRes: Int

    data object Sunny : CompassDialShape {
        override val shape: Shape = SunnyShape(lobes = 8, depthRatio = 0.12f)
        override val nameRes: Int = R.string.dial_shape_sunny
    }

    data object Circle : CompassDialShape {
        override val shape: Shape = CircleShape
        override val nameRes: Int = R.string.dial_shape_circle
    }

    data object Diamond : CompassDialShape {
        override val shape: Shape = DiamondShape(depthRatio = 0.18f)
        override val nameRes: Int = R.string.dial_shape_diamond
    }

    data object Octagon : CompassDialShape {
        override val shape: Shape = OctagonShape(cornerRoundingFraction = 0.45f)
        override val nameRes: Int = R.string.dial_shape_octagon
    }
}

/**
 * Global settings state for the Compass Dial configuration.
 */
object CompassDialSettings {
    var currentShape: CompassDialShape by mutableStateOf(CompassDialShape.Sunny)

    val availableShapes: List<CompassDialShape> = listOf(
        CompassDialShape.Sunny,
        CompassDialShape.Circle,
        CompassDialShape.Diamond,
        CompassDialShape.Octagon
    )
}
