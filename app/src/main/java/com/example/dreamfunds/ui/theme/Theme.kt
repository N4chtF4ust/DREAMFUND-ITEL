// app/src/main/java/com/example/dreamfunds/ui/theme/Theme.kt
package com.example.dreamfunds.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DreamFundsDarkColorScheme = darkColorScheme(
    primary = Color(0xFF82B1FF),
    onPrimary = Color(0xFF00205C),
    primaryContainer = Color(0xFF003082),
    onPrimaryContainer = Color(0xFFD9E2FF),
    secondary = Color(0xFFBBC4FF),
    onSecondary = Color(0xFF232B60),
    secondaryContainer = Color(0xFF3A4278),
    onSecondaryContainer = Color(0xFFDFE0FF),
    background = Color(0xFF1A1C22),
    onBackground = Color(0xFFE3E2E9),
    surface = Color(0xFF1A1C22),
    onSurface = Color(0xFFE3E2E9),
    surfaceVariant = Color(0xFF44464F),
    onSurfaceVariant = Color(0xFFC5C6D0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val DreamFundsLightColorScheme = lightColorScheme(
    primary = Color(0xFF1A56DB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E2FF),
    onPrimaryContainer = Color(0xFF00174B),
    secondary = Color(0xFF525DAD),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDFE0FF),
    onSecondaryContainer = Color(0xFF0C1663),
    background = Color(0xFFFBF8FF),
    onBackground = Color(0xFF1A1C22),
    surface = Color(0xFFFBF8FF),
    onSurface = Color(0xFF1A1C22),
    surfaceVariant = Color(0xFFE3E2EC),
    onSurfaceVariant = Color(0xFF46464F),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun DreamFundsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DreamFundsDarkColorScheme
        else -> DreamFundsLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}