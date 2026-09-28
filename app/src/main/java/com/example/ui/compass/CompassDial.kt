package com.example.ui.compass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private enum class MarkerCategory {
    CARDINAL,   // N, S, W, E (scaled ~12% larger to 27.sp)
    DIAGONAL    // NE, SE, SW, NW (unchanged at 24.sp)
}

private data class DirectionMarker(
    val label: String,
    val angleDegrees: Float,
    val category: MarkerCategory
)

// All 8 outer directions forming a mathematically symmetrical outer ring
private val DIRECTION_MARKERS = listOf(
    DirectionMarker("N", 0f, MarkerCategory.CARDINAL),
    DirectionMarker("NE", 45f, MarkerCategory.DIAGONAL),
    DirectionMarker("E", 90f, MarkerCategory.CARDINAL),
    DirectionMarker("SE", 135f, MarkerCategory.DIAGONAL),
    DirectionMarker("S", 180f, MarkerCategory.CARDINAL),
    DirectionMarker("SW", 225f, MarkerCategory.DIAGONAL),
    DirectionMarker("W", 270f, MarkerCategory.CARDINAL),
    DirectionMarker("NW", 315f, MarkerCategory.DIAGONAL)
)

// Shared TextStyle for the 4 outer cardinal direction labels (N, S, W, E) - ~12% increase from 24.sp
private val OuterCardinalDirectionTextStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 27.sp,
    lineHeight = 31.sp,
    letterSpacing = 0.sp
)

// Shared TextStyle for the 4 outer diagonal direction labels (NE, NW, SE, SW) - unchanged at 24.sp
private val OuterDiagonalDirectionTextStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Medium,
    fontSize = 24.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp
)

/**
 * Reusable, scalable Compass Dial component.
 *
 * @param shape The outer dial shape (default is Sunny).
 * @param rotationDegrees Rotation angle in degrees for future sensor linkage.
 * @param headingDegrees Current heading in degrees (0..360).
 * @param currentDirection Current cardinal/intercardinal direction name.
 */
@Composable
fun CompassDial(
    modifier: Modifier = Modifier,
    shape: CompassDialShape = CompassDialShape.Sunny,
    rotationDegrees: Float = 0f,
    headingDegrees: Float = 0f,
    currentDirection: String = "N"
) {
    Surface(
        shape = shape.shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        tonalElevation = 2.dp,
        modifier = modifier.testTag("compass_dial_surface")
    ) {
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            val sizePx = constraints.maxWidth.toFloat()
            val radiusPx = sizePx / 2f

            // 1. All 8 Directional Labels positioned by angle around the dial with shape-aware safe margins
            DIRECTION_MARKERS.forEach { marker ->
                val radialDistancePx = when (shape) {
                    CompassDialShape.Diamond -> {
                        when (marker.category) {
                            MarkerCategory.CARDINAL -> radiusPx * 0.77f
                            MarkerCategory.DIAGONAL -> radiusPx * 0.59f
                        }
                    }
                    CompassDialShape.Octagon -> radiusPx * 0.72f
                    else -> radiusPx * 0.77f
                }

                val angleRad = Math.toRadians((marker.angleDegrees - rotationDegrees).toDouble())
                val xOffset = (radialDistancePx * sin(angleRad)).roundToInt()
                val yOffset = (-radialDistancePx * cos(angleRad)).roundToInt()

                val textStyle = when (marker.category) {
                    MarkerCategory.CARDINAL -> OuterCardinalDirectionTextStyle
                    MarkerCategory.DIAGONAL -> OuterDiagonalDirectionTextStyle
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset { IntOffset(xOffset, yOffset) }
                        .testTag("dial_label_${marker.label}")
                ) {
                    Text(
                        text = marker.label,
                        style = textStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 2. Central Readout Area: ONE single parent container centered inside the dial
            // The central vertical axis is established strictly by the digit "0".
            // The degree symbol "°" sits naturally attached to the upper-right without shifting the center axis of N and arrow.
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("compass_center_readout")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Degree value: "0" digit establishes the center axis; "°" is naturally attached to upper-right
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${headingDegrees.toInt()}",
                            fontSize = 44.sp,
                            lineHeight = 48.sp,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = (-0.5).sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "°",
                            fontSize = 27.sp,
                            lineHeight = 27.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 8.dp, y = 0.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Center direction letter aligned directly with the center of the "0" digit
                    Text(
                        text = currentDirection,
                        fontSize = 28.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Compass Pointer arrow directly below N on the exact same center axis
                    CompassPointer(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(width = 16.dp, height = 20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Minimal, clean compass pointer shape.
 * Mathematically symmetrical on the horizontal X center axis.
 */
@Composable
private fun CompassPointer(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w / 2f, 0f) // Top tip on exact horizontal center
            lineTo(w, h) // Bottom right
            lineTo(w / 2f, h * 0.78f) // Inner notch on exact horizontal center
            lineTo(0f, h) // Bottom left
            close()
        }

        drawPath(path = path, color = color)
    }
}
