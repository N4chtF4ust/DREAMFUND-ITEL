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
    primary = Color(0xFF8BED9F),
    onPrimary = Color(0xFF003916),
    primaryContainer = Color(0xFF005223),
    onPrimaryContainer = Color(0xFFA6F5B9),
    secondary = Color(0xFFB5CCB8),
    onSecondary = Color(0xFF223526),
    secondaryContainer = Color(0xFF384B3C),
    onSecondaryContainer = Color(0xFFD1E8D3),
    background = Color(0xFF111411),
    onBackground = Color(0xFFE1E3DF),
    surface = Color(0xFF111411),
    onSurface = Color(0xFFE1E3DF),
    surfaceVariant = Color(0xFF414941),
    onSurfaceVariant = Color(0xFFC1C9BF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val DreamFundsLightColorScheme = lightColorScheme(
    primary = Color(0xFF1B6B35), // A strong, readable Emerald/Forest Green
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA6F5B9), // Soft mint for containers
    onPrimaryContainer = Color(0xFF00210A),
    secondary = Color(0xFF4F6353), // Muted sage green for secondary elements
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1E8D3),
    onSecondaryContainer = Color(0xFF0C1F12),
    background = Color(0xFFFBFDF8),
    onBackground = Color(0xFF191C19),
    surface = Color(0xFFFBFDF8),
    onSurface = Color(0xFF191C19),
    surfaceVariant = Color(0xFFDDE5DB),
    onSurfaceVariant = Color(0xFF414941),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)
@Composable
fun DreamFundsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
            window.statusBarColor = if (darkTheme) Color.Black.toArgb() else colorScheme.onPrimary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}