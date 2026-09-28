package com.example.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Global theme configuration state accessible across the app.
 */
object AppThemeState {
    var dynamicColorEnabled by mutableStateOf(true)
    var themeMode by mutableStateOf(ThemeMode.SYSTEM)
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

private val DarkColorScheme = darkColorScheme(
    primary = MdDarkPrimary,
    onPrimary = MdDarkOnPrimary,
    primaryContainer = MdDarkPrimaryContainer,
    onPrimaryContainer = MdDarkOnPrimaryContainer,
    secondary = MdDarkSecondary,
    onSecondary = MdDarkOnSecondary,
    secondaryContainer = MdDarkSecondaryContainer,
    onSecondaryContainer = MdDarkOnSecondaryContainer,
    tertiary = MdDarkTertiary,
    onTertiary = MdDarkOnTertiary,
    tertiaryContainer = MdDarkTertiaryContainer,
    onTertiaryContainer = MdDarkOnTertiaryContainer,
    error = MdDarkError,
    onError = MdDarkOnError,
    errorContainer = MdDarkErrorContainer,
    onErrorContainer = MdDarkOnErrorContainer,
    background = MdDarkBackground,
    onBackground = MdDarkOnBackground,
    surface = MdDarkSurface,
    onSurface = MdDarkOnSurface,
    surfaceVariant = MdDarkSurfaceVariant,
    onSurfaceVariant = MdDarkOnSurfaceVariant,
    outline = MdDarkOutline,
    outlineVariant = MdDarkOutlineVariant,
    surfaceContainerLowest = MdDarkSurfaceContainerLowest,
    surfaceContainerLow = MdDarkSurfaceContainerLow,
    surfaceContainer = MdDarkSurfaceContainer,
    surfaceContainerHigh = MdDarkSurfaceContainerHigh,
    surfaceContainerHighest = MdDarkSurfaceContainerHighest
)

private val LightColorScheme = lightColorScheme(
    primary = MdLightPrimary,
    onPrimary = MdLightOnPrimary,
    primaryContainer = MdLightPrimaryContainer,
    onPrimaryContainer = MdLightOnPrimaryContainer,
    secondary = MdLightSecondary,
    onSecondary = MdLightOnSecondary,
    secondaryContainer = MdLightSecondaryContainer,
    onSecondaryContainer = MdLightOnSecondaryContainer,
    tertiary = MdLightTertiary,
    onTertiary = MdLightOnTertiary,
    tertiaryContainer = MdLightTertiaryContainer,
    onTertiaryContainer = MdLightOnTertiaryContainer,
    error = MdLightError,
    onError = MdLightOnError,
    errorContainer = MdLightErrorContainer,
    onErrorContainer = MdLightOnErrorContainer,
    background = MdLightBackground,
    onBackground = MdLightOnBackground,
    surface = MdLightSurface,
    onSurface = MdLightOnSurface,
    surfaceVariant = MdLightSurfaceVariant,
    onSurfaceVariant = MdLightOnSurfaceVariant,
    outline = MdLightOutline,
    outlineVariant = MdLightOutlineVariant,
    surfaceContainerLowest = MdLightSurfaceContainerLowest,
    surfaceContainerLow = MdLightSurfaceContainerLow,
    surfaceContainer = MdLightSurfaceContainer,
    surfaceContainerHigh = MdLightSurfaceContainerHigh,
    surfaceContainerHighest = MdLightSurfaceContainerHighest
)

/**
 * Interpolates between a base surface color and a tint color for subtle Material You tonal washes.
 */
private fun blendColor(base: Color, tint: Color, factor: Float): Color {
    val f = factor.coerceIn(0f, 1f)
    return Color(
        red = base.red * (1f - f) + tint.red * f,
        green = base.green * (1f - f) + tint.green * f,
        blue = base.blue * (1f - f) + tint.blue * f,
        alpha = 1f
    )
}

/**
 * Builds the Dynamic Light color scheme using Android 12+ Monet engine
 * with a subtle ~10–12% wallpaper-derived background tint and ~15–18% dial/container tint,
 * ensuring the light theme never appears as static/pure white #FFFFFF.
 */
private fun buildDynamicLightColorScheme(context: Context): ColorScheme {
    val dynamic = dynamicLightColorScheme(context)
    val dynamicPrimary = dynamic.primary

    // Subtle Material You tonal wash over calm light neutral bases
    val baseNeutralBg = Color(0xFFF5F6FA)
    val tintedBg = blendColor(baseNeutralBg, dynamicPrimary, 0.05f) // ~10–12% perceived visual tint
    val tintedSurface = blendColor(baseNeutralBg, dynamicPrimary, 0.05f)
    val tintedContainerLowest = Color(0xFFFFFFFF)
    val tintedContainerLow = blendColor(Color(0xFFEBECEF), dynamicPrimary, 0.09f) // ~15% visual tint for Dial
    val tintedContainer = blendColor(Color(0xFFE4E6EA), dynamicPrimary, 0.11f)    // ~18% visual tint for Settings Cards
    val tintedContainerHigh = blendColor(Color(0xFFDDDFE5), dynamicPrimary, 0.13f) // ~20% visual tint for Dialogs
    val tintedContainerHighest = blendColor(Color(0xFFD6D9E0), dynamicPrimary, 0.15f)
    val tintedSurfaceVariant = blendColor(Color(0xFFDEE1EB), dynamicPrimary, 0.08f)
    val tintedOutlineVariant = blendColor(Color(0xFFC3C7D2), dynamicPrimary, 0.06f)

    return dynamic.copy(
        background = tintedBg,
        surface = tintedSurface,
        surfaceContainerLowest = tintedContainerLowest,
        surfaceContainerLow = tintedContainerLow,
        surfaceContainer = tintedContainer,
        surfaceContainerHigh = tintedContainerHigh,
        surfaceContainerHighest = tintedContainerHighest,
        surfaceVariant = tintedSurfaceVariant,
        outlineVariant = tintedOutlineVariant
    )
}

/**
 * Builds the Dynamic Dark color scheme using the Android 12+ Monet engine
 * with an extra-deep AMOLED-black background while preserving the wallpaper-derived Material You dynamic tint.
 */
private fun buildDynamicDarkColorScheme(context: Context): ColorScheme {
    val dynamic = dynamicDarkColorScheme(context)
    val dynamicPrimary = dynamic.primary

    // Main & Settings background: 5–10% deeper AMOLED-black base with subtly visible wallpaper-derived dynamic tint
    val backgroundAmoledBase = Color(0xFF010204)
    val tintedBg = blendColor(backgroundAmoledBase, dynamicPrimary, 0.12f)
    val tintedSurface = blendColor(backgroundAmoledBase, dynamicPrimary, 0.12f)

    // Unchanged container and elevated surfaces preserving exact tonal hierarchy
    val tintedContainerLowest = Color(0xFF000102)
    val tintedContainerLow = blendColor(Color(0xFF07080E), dynamicPrimary, 0.18f)      // Compass Dial surface (unchanged)
    val tintedContainer = blendColor(Color(0xFF0D1017), dynamicPrimary, 0.18f)         // Settings grouped containers (unchanged)
    val tintedContainerHigh = blendColor(Color(0xFF141720), dynamicPrimary, 0.18f)     // Elevated dialogs (unchanged)
    val tintedContainerHighest = blendColor(Color(0xFF1A1E28), dynamicPrimary, 0.18f)  // Highest elevation overlays (unchanged)
    val tintedSurfaceVariant = blendColor(Color(0xFF151922), dynamicPrimary, 0.18f)
    val tintedOutlineVariant = blendColor(Color(0xFF1E222D), dynamicPrimary, 0.15f)

    return dynamic.copy(
        background = tintedBg,
        surface = tintedSurface,
        surfaceContainerLowest = tintedContainerLowest,
        surfaceContainerLow = tintedContainerLow,
        surfaceContainer = tintedContainer,
        surfaceContainerHigh = tintedContainerHigh,
        surfaceContainerHighest = tintedContainerHighest,
        surfaceVariant = tintedSurfaceVariant,
        outlineVariant = tintedOutlineVariant
    )
}

@Composable
fun CompassTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) buildDynamicDarkColorScheme(context) else buildDynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(
        LocalSpacing provides Spacing()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

object CompassThemeTokens {
    val spacing: Spacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSpacing.current
}
