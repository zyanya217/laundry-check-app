package io.github.zyanya217.laundrycheck.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import io.github.zyanya217.laundrycheck.model.ThemeChoice

/** Resolves the person's light/dark/system choice to a dark-or-not flag. */
@Composable
fun ThemeChoice.isDark(): Boolean = when (this) {
    ThemeChoice.AUTO -> isSystemInDarkTheme()
    ThemeChoice.LIGHT -> false
    ThemeChoice.DARK -> true
}

/**
 * Material 3 with dynamic colour (always available: minSdk is 33), the embedded fonts, and the fixed
 * verdict colours from [LaundryColors].
 */
@Composable
fun LaundryTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colorScheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

    // The in-app light/dark choice can differ from the system's, so set the system bar icons to match.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalLaundryColors provides if (darkTheme) DarkLaundryColors else LightLaundryColors) {
        MaterialTheme(colorScheme = colorScheme, typography = LaundryTypography, content = content)
    }
}
