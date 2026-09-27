package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CoralAccent,
    onPrimary = Color.White,
    primaryContainer = CoralDarkCard,
    onPrimaryContainer = CoralCard,
    secondary = SkyBlueAccent,
    onSecondary = Color.White,
    secondaryContainer = SkyBlueDarkCard,
    onSecondaryContainer = SkyBlueCard,
    tertiary = MatchaMintAccent,
    onTertiary = Color.White,
    background = DeepMatteSlate,
    onBackground = Color(0xFFF8FAFC),
    surface = DeepMatteSlateCard,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = DeepMatteSlateBorder,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569)
)

private val LightColorScheme = lightColorScheme(
    primary = CoralAccent,
    onPrimary = Color.White,
    primaryContainer = CoralCard,
    onPrimaryContainer = CoralAccent,
    secondary = SkyBlueAccent,
    onSecondary = Color.White,
    secondaryContainer = SkyBlueCard,
    onSecondaryContainer = SkyBlueAccent,
    tertiary = MatchaMintAccent,
    onTertiary = Color.White,
    background = WarmRicePaper,
    onBackground = Color(0xFF0F172A),
    surface = WarmRicePaperElevated,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = PebbleLight,
    onSurfaceVariant = Color(0xFF475569),
    outline = PebbleBorderLight
)

@Composable
fun PebbleFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Keep alias for backwards compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    PebbleFlowTheme(darkTheme = darkTheme, content = content)
}
