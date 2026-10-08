package com.digitalbalance.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = StitchPrimaryIndigo,
    onPrimary = TextWhitePrimary,
    primaryContainer = DarkNavySurfaceVariant,
    onPrimaryContainer = TextWhitePrimary,
    secondary = StitchAccentEmerald,
    onSecondary = TextWhitePrimary,
    secondaryContainer = DarkNavySurfaceVariant,
    onSecondaryContainer = StitchAccentEmerald,
    tertiary = StitchAccentTeal,
    background = DarkNavyBackground,
    onBackground = TextWhitePrimary,
    surface = DarkNavySurface,
    onSurface = TextWhitePrimary,
    surfaceVariant = DarkNavySurfaceVariant,
    onSurfaceVariant = TextMutedSecondary,
    outline = DarkNavyBorder
)

private val LightColorScheme = lightColorScheme(
    primary = StitchPrimaryIndigo,
    onPrimary = TextWhitePrimary,
    primaryContainer = DarkNavySurfaceVariant,
    onPrimaryContainer = TextWhitePrimary,
    secondary = StitchAccentEmerald,
    onSecondary = TextWhitePrimary,
    secondaryContainer = DarkNavySurfaceVariant,
    onSecondaryContainer = StitchAccentEmerald,
    tertiary = StitchAccentTeal,
    background = DarkNavyBackground,
    onBackground = TextWhitePrimary,
    surface = DarkNavySurface,
    onSurface = TextWhitePrimary,
    surfaceVariant = DarkNavySurfaceVariant,
    onSurfaceVariant = TextMutedSecondary,
    outline = DarkNavyBorder
)

@Composable
fun DigitalBalanceTheme(
    darkTheme: Boolean = true, // Default to Stitch Dark Wellbeing Theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

