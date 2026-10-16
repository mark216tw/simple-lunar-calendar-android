package com.simplelunar.calendar.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.simplelunar.calendar.data.DisplayMode
import com.simplelunar.calendar.data.ThemeChoice
import com.simplelunar.calendar.data.ThemeSettings

private val WarmLightBackground = Color(0xFFFFF9F4)
private val WarmDarkBackground = Color(0xFF171311)

fun resolvePrimary(settings: ThemeSettings): Color = when (settings.colorChoice) {
    ThemeChoice.RED -> Color(0xFFD0403B)
    ThemeChoice.PINK -> Color(0xFFD9636F)
    ThemeChoice.PEACH -> Color(0xFFC76A44)
    ThemeChoice.BERRY -> Color(0xFFBE5278)
    ThemeChoice.ORANGE -> Color(0xFFD97428)
    ThemeChoice.GOLD -> Color(0xFFB5851A)
    ThemeChoice.CUSTOM -> Color.hsl(settings.customHue, 0.64f, 0.43f)
}

fun selectedContentColor(background: Color): Color =
    if (background.luminance() > 0.42f) Color(0xFF17120F) else Color.White

@Composable
fun SimpleLunarTheme(
    settings: ThemeSettings,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (settings.displayMode) {
        DisplayMode.SYSTEM -> systemDark
        DisplayMode.LIGHT -> false
        DisplayMode.DARK -> true
    }
    val basePrimary = resolvePrimary(settings)
    val primary = if (darkTheme) lerp(basePrimary, Color.White, 0.18f) else basePrimary
    val onPrimary = selectedContentColor(primary)

    val colors = if (darkTheme) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = lerp(basePrimary, Color.Black, 0.45f),
            onPrimaryContainer = Color(0xFFFFE6DD),
            background = WarmDarkBackground,
            onBackground = Color(0xFFF4EDEA),
            surface = Color(0xFF211B18),
            onSurface = Color(0xFFF4EDEA),
            surfaceVariant = Color(0xFF322A26),
            onSurfaceVariant = Color(0xFFD9C8C0),
            outline = Color(0xFF9D8B83),
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = lerp(basePrimary, Color.White, 0.84f),
            onPrimaryContainer = lerp(basePrimary, Color.Black, 0.52f),
            background = WarmLightBackground,
            onBackground = Color(0xFF271A16),
            surface = Color(0xFFFFFCFA),
            onSurface = Color(0xFF271A16),
            surfaceVariant = Color(0xFFF3E8E2),
            onSurfaceVariant = Color(0xFF594841),
            outline = Color(0xFF88736A),
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(colorScheme = colors, content = content)
}
