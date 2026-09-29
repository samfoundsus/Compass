package com.example.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.ui.compass.CompassDialShape
import com.example.ui.theme.AppThemeState
import com.example.ui.theme.MdDarkPrimary
import com.example.ui.theme.MdLightPrimary
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.blendColor
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * High-quality bitmap renderer for the Native Compass AppWidget.
 * Reuses the exact mathematical geometry, direction label positioning, upright text behavior,
 * and Material You dynamic color / AMOLED dark theme treatment from the main Compass app.
 */
object CompassWidgetRenderer {

    private const val BITMAP_SIZE = 512

    fun renderDialBitmap(
        context: Context,
        shape: CompassDialShape,
        rotationDegrees: Float = 0f,
        headingDegrees: Float = 0f,
        currentDirection: String = "N"
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(BITMAP_SIZE, BITMAP_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val isDark = when (AppThemeState.themeMode) {
            ThemeMode.SYSTEM -> (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }

        // 1. Resolve Dynamic / Themed Colors
        val dynamicPrimary = if (AppThemeState.dynamicColorEnabled && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            runCatching {
                if (isDark) dynamicDarkColorScheme(context).primary else dynamicLightColorScheme(context).primary
            }.getOrDefault(if (isDark) MdDarkPrimary else MdLightPrimary)
        } else {
            if (isDark) MdDarkPrimary else MdLightPrimary
        }

        val dialBgColorArgb = if (isDark) {
            blendColor(Color(0xFF07080E), dynamicPrimary, 0.18f).toArgb()
        } else {
            blendColor(Color(0xFFF0F3FA), dynamicPrimary, 0.05f).toArgb()
        }

        val outlineColorArgb = if (isDark) {
            blendColor(Color(0xFF1E222D), dynamicPrimary, 0.15f).copy(alpha = 0.35f).toArgb()
        } else {
            blendColor(Color(0xFFD6D9E0), dynamicPrimary, 0.05f).copy(alpha = 0.35f).toArgb()
        }

        val onSurfaceArgb = if (isDark) {
            Color(0xFFE2E2E9).toArgb()
        } else {
            Color(0xFF191C20).toArgb()
        }

        val onSurfaceVariantArgb = if (isDark) {
            Color(0xFFC4C6D0).toArgb()
        } else {
            Color(0xFF44474E).toArgb()
        }

        val primaryArgb = dynamicPrimary.toArgb()

        // 2. Setup Paints
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = dialBgColorArgb
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = outlineColorArgb
            strokeWidth = 2.5f
        }

        val cardinalTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = onSurfaceVariantArgb
            textSize = BITMAP_SIZE * 0.076f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val diagonalTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = onSurfaceVariantArgb
            textSize = BITMAP_SIZE * 0.066f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }

        val degreeValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = onSurfaceArgb
            textSize = BITMAP_SIZE * 0.122f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            letterSpacing = -0.02f
        }

        val degreeSymbolPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = onSurfaceArgb
            textSize = BITMAP_SIZE * 0.074f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.LEFT
        }

        val directionTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = onSurfaceVariantArgb
            textSize = BITMAP_SIZE * 0.078f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryArgb
            style = Paint.Style.FILL
        }

        // 3. Layout & Geometry Calculations
        val padding = BITMAP_SIZE * 0.035f
        val dialSize = BITMAP_SIZE - 2 * padding
        val radius = dialSize / 2f
        val cx = BITMAP_SIZE / 2f
        val cy = BITMAP_SIZE / 2f

        // 4. Draw Dial Background & Border (with dial rotation)
        val dialPath = CompassDialGeometry.createPathForShape(shape, dialSize, dialSize)
        val matrix = android.graphics.Matrix().apply {
            postTranslate(cx - radius, cy - radius)
            postRotate(-rotationDegrees, cx, cy)
        }
        dialPath.transform(matrix)

        canvas.drawPath(dialPath, fillPaint)
        canvas.drawPath(dialPath, strokePaint)

        // 5. Draw 8 Directional Markers (N, NE, E, SE, S, SW, W, NW)
        // Position rotates with dial, but text remains upright!
        CompassDialGeometry.DIRECTION_MARKERS.forEach { marker ->
            val radialDist = CompassDialGeometry.getRadialDistance(shape, radius, marker.category)
            val netAngleDeg = marker.angleDegrees - rotationDegrees
            val angleRad = Math.toRadians(netAngleDeg.toDouble())
            val markerX = cx + (radialDist * sin(angleRad)).toFloat()
            val markerY = cy - (radialDist * cos(angleRad)).toFloat()

            val textPaint = if (marker.category == MarkerCategory.CARDINAL) cardinalTextPaint else diagonalTextPaint
            val verticalOffset = (textPaint.descent() + textPaint.ascent()) / 2f

            canvas.drawText(marker.label, markerX, markerY - verticalOffset, textPaint)
        }

        // 6. Draw Fixed Central Readout ("0°", "N", pointer arrow)
        val degreeStr = headingDegrees.toInt().toString()
        val degreeValWidth = degreeValuePaint.measureText(degreeStr)
        val degreeY = cy - BITMAP_SIZE * 0.045f

        // Centered "0" degree digit
        canvas.drawText(degreeStr, cx, degreeY, degreeValuePaint)

        // "°" symbol attached to top-right of degree value
        val symbolX = cx + (degreeValWidth / 2f) + (BITMAP_SIZE * 0.012f)
        val symbolY = degreeY - (BITMAP_SIZE * 0.040f)
        canvas.drawText("°", symbolX, symbolY, degreeSymbolPaint)

        // Center direction text
        val dirY = cy + (BITMAP_SIZE * 0.045f)
        canvas.drawText(currentDirection, cx, dirY, directionTextPaint)

        // Pointer Arrow
        val arrowTop = dirY + (BITMAP_SIZE * 0.020f)
        val arrowW = BITMAP_SIZE * 0.042f
        val arrowH = BITMAP_SIZE * 0.052f

        val arrowPath = Path().apply {
            moveTo(cx, arrowTop)
            lineTo(cx + arrowW / 2f, arrowTop + arrowH)
            lineTo(cx, arrowTop + arrowH * 0.78f)
            lineTo(cx - arrowW / 2f, arrowTop + arrowH)
            close()
        }
        canvas.drawPath(arrowPath, pointerPaint)

        return bitmap
    }
}
