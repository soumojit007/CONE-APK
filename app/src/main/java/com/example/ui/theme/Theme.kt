package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ElegantDarkColorScheme = darkColorScheme(
    primary = ElegantDarkPrimary,
    onPrimary = ElegantDarkOnPrimary,
    primaryContainer = ElegantDarkSurfaceCardAlt,
    onPrimaryContainer = ElegantDarkPrimary,
    secondary = ElegantDarkSecondary,
    onSecondary = ElegantDarkBg,
    secondaryContainer = ElegantDarkHeaderContainer,
    onSecondaryContainer = ElegantDarkPrimary,
    background = ElegantDarkBg,
    onBackground = ElegantDarkText,
    surface = ElegantDarkSurface,
    onSurface = ElegantDarkText,
    surfaceVariant = ElegantDarkSurfaceCard,
    onSurfaceVariant = ElegantDarkSecondary,
    outline = ElegantDarkOutline,
    outlineVariant = ElegantDarkOutlineVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // We default to Elegant Dark theme!
    dynamicColor: Boolean = false, // Disable device dynamic overlays to preserve curated layout
    content: @Composable () -> Unit,
) {
    // Under the requested "Elegant Dark" guide, we enforce the ElegantDarkColorScheme exclusively
    MaterialTheme(
        colorScheme = ElegantDarkColorScheme,
        typography = Typography,
        content = content
    )
}
