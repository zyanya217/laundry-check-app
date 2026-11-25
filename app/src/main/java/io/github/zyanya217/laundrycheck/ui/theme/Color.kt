package io.github.zyanya217.laundrycheck.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The colours that carry meaning (good, borderline, bad, rain, humidity). They stay fixed, copied from
 * the web app's CSS, while the surfaces and text follow the wallpaper through Material dynamic colour:
 * a "green means hang it out" cue must not turn red because the wallpaper is red.
 */
@Immutable
class LaundryColors(
    val ok: Color,
    val okBg: Color,
    val mid: Color,
    val midBg: Color,
    val bad: Color,
    val badBg: Color,
    val rain: Color,
    val humid: Color,
)

internal val LightLaundryColors = LaundryColors(
    ok = Color(0xFF1A7F52),
    okBg = Color(0xFFDCF2E6),
    mid = Color(0xFFA85A00),
    midBg = Color(0xFFFAECC6),
    bad = Color(0xFFC0392B),
    badBg = Color(0xFFF8DBD5),
    rain = Color(0xFF3573D6),
    humid = Color(0xFF7A56C6),
)

internal val DarkLaundryColors = LaundryColors(
    ok = Color(0xFF4FC38A),
    okBg = Color(0xFF123A2A),
    mid = Color(0xFFF0B24A),
    midBg = Color(0xFF3A2D0C),
    bad = Color(0xFFFF8D7E),
    badBg = Color(0xFF431B15),
    rain = Color(0xFF8FB7FF),
    humid = Color(0xFFB49AF0),
)

internal val LocalLaundryColors = staticCompositionLocalOf { LightLaundryColors }

val MaterialTheme.laundry: LaundryColors
    @Composable
    @ReadOnlyComposable
    get() = LocalLaundryColors.current
