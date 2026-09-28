package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ==============================================================================
// Light Color Scheme Tokens (Google Material 3 / Pixel Tinted Light Style)
// Clear tonal hierarchy: Background ≈ 10-15% tint, Dial / Containers ≈ 15-20% tint
// ==============================================================================
val MdLightPrimary = Color(0xFF285EA8)
val MdLightOnPrimary = Color(0xFFFFFFFF)
val MdLightPrimaryContainer = Color(0xFFD7E2FF)
val MdLightOnPrimaryContainer = Color(0xFF001A40)

val MdLightSecondary = Color(0xFF565E71)
val MdLightOnSecondary = Color(0xFFFFFFFF)
val MdLightSecondaryContainer = Color(0xFFDAE2F9)
val MdLightOnSecondaryContainer = Color(0xFF131C2B)

val MdLightTertiary = Color(0xFF705574)
val MdLightOnTertiary = Color(0xFFFFFFFF)
val MdLightTertiaryContainer = Color(0xFFFAD7FB)
val MdLightOnTertiaryContainer = Color(0xFF28132D)

val MdLightError = Color(0xFFBA1A1A)
val MdLightOnError = Color(0xFFFFFFFF)
val MdLightErrorContainer = Color(0xFFFFDAD6)
val MdLightOnErrorContainer = Color(0xFF410002)

// Main light background & surface (~10–12% subtle soft tint, predominantly light and neutral)
val MdLightBackground = Color(0xFFF3F4F8)
val MdLightOnBackground = Color(0xFF191C20)
val MdLightSurface = Color(0xFFF3F4F8)
val MdLightOnSurface = Color(0xFF191C20)
val MdLightSurfaceVariant = Color(0xFFDFE2EB)
val MdLightOnSurfaceVariant = Color(0xFF43474E)
val MdLightOutline = Color(0xFF74777F)
val MdLightOutlineVariant = Color(0xFFC4C6D0)

// Light Tonal Surface Container Hierarchy (~15–20% tint on containers/dial)
val MdLightSurfaceContainerLowest = Color(0xFFFFFFFF)
val MdLightSurfaceContainerLow = Color(0xFFE9EBF1)      // Compass Dial surface (~15% tint)
val MdLightSurfaceContainer = Color(0xFFE2E5EC)         // Settings grouped cards (~18% tint)
val MdLightSurfaceContainerHigh = Color(0xFFDCDEE6)     // Elevated selection dialogs (~20% tint)
val MdLightSurfaceContainerHighest = Color(0xFFD6D9E0)  // Highest elevation overlays

// ==============================================================================
// Dark Color Scheme Tokens — Refined Dark Charcoal with Clear M3 Tonal Hierarchy (~5% deeper)
// Soft, refined dark theme avoiding pure/near-black AMOLED darkness
// ==============================================================================
val MdDarkPrimary = Color(0xFFA8C7FA)
val MdDarkOnPrimary = Color(0xFF083063)
val MdDarkPrimaryContainer = Color(0xFF204270)
val MdDarkOnPrimaryContainer = Color(0xFFD3E3FD)

val MdDarkSecondary = Color(0xFFBCC7DB)
val MdDarkOnSecondary = Color(0xFF263140)
val MdDarkSecondaryContainer = Color(0xFF394252)
val MdDarkOnSecondaryContainer = Color(0xFFD8E3F8)

val MdDarkTertiary = Color(0xFFD7BEE4)
val MdDarkOnTertiary = Color(0xFF3B2948)
val MdDarkTertiaryContainer = Color(0xFF4D3B59)
val MdDarkOnTertiaryContainer = Color(0xFFF3DAFF)

val MdDarkError = Color(0xFFFFB4AB)
val MdDarkOnError = Color(0xFF690005)
val MdDarkErrorContainer = Color(0xFF93000A)
val MdDarkOnErrorContainer = Color(0xFFFFDAD6)

// Base background & surface: refined dark charcoal (~5% darker than 0xFF1B1D22)
val MdDarkBackground = Color(0xFF181A1F)
val MdDarkOnBackground = Color(0xFFE2E2E9)
val MdDarkSurface = Color(0xFF181A1F)
val MdDarkOnSurface = Color(0xFFE2E2E9)
val MdDarkSurfaceVariant = Color(0xFF3F434A)
val MdDarkOnSurfaceVariant = Color(0xFFC4C6D0)
val MdDarkOutline = Color(0xFF868A91)
val MdDarkOutlineVariant = Color(0xFF3F434A)

// Dark Tonal Surface Hierarchy (~5% deeper, distinguishable, calm steps)
val MdDarkSurfaceContainerLowest = Color(0xFF131519)
val MdDarkSurfaceContainerLow = Color(0xFF1E2026)       // Used for Sunny compass dial
val MdDarkSurfaceContainer = Color(0xFF23252B)          // Used for Settings rounded containers (~5% darker)
val MdDarkSurfaceContainerHigh = Color(0xFF2E3138)      // Used for dialogs and elevated elements
val MdDarkSurfaceContainerHighest = Color(0xFF393C43)   // Used for highest elevation overlays
