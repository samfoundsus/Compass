package com.example.ui.compass

import androidx.annotation.StringRes
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.example.R
import com.example.widget.CompassDialGeometry

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
        val androidPath = CompassDialGeometry.createSunnyPath(size.width, size.height, lobes, depthRatio)
        return Outline.Generic(androidPath.asComposePath())
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
        val androidPath = CompassDialGeometry.createDiamondPath(size.width, size.height, depthRatio)
        return Outline.Generic(androidPath.asComposePath())
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
        val androidPath = CompassDialGeometry.createOctagonPath(size.width, size.height, cornerRoundingFraction)
        return Outline.Generic(androidPath.asComposePath())
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
