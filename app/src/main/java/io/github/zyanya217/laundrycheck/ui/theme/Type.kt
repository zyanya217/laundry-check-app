package io.github.zyanya217.laundrycheck.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.zyanya217.laundrycheck.R
import androidx.compose.ui.text.ExperimentalTextApi

/** The three families the web app uses, embedded (all SIL OFL 1.1; see /licenses/fonts). */
@OptIn(ExperimentalTextApi::class)
object LaundryFonts {
    /** Headlines. A single weight. */
    val Display = FontFamily(Font(R.font.huninn_regular))

    /** Body text. One variable font file serves all three weights. */
    val Sans = FontFamily(
        Font(R.font.noto_sans_tc, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
        Font(R.font.noto_sans_tc, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
        Font(R.font.noto_sans_tc, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    )

    /** Times, numbers and small labels. */
    val Mono = FontFamily(
        Font(R.font.dm_mono_regular, FontWeight.Normal),
        Font(R.font.dm_mono_medium, FontWeight.Medium),
    )
}

/** Material's default scale with Noto Sans TC everywhere and the web app's 15 px body size. */
internal val LaundryTypography: Typography = Typography().let { d ->
    val f = LaundryFonts.Sans
    d.copy(
        displayLarge = d.displayLarge.copy(fontFamily = f),
        displayMedium = d.displayMedium.copy(fontFamily = f),
        displaySmall = d.displaySmall.copy(fontFamily = f),
        headlineLarge = d.headlineLarge.copy(fontFamily = f),
        headlineMedium = d.headlineMedium.copy(fontFamily = f),
        headlineSmall = d.headlineSmall.copy(fontFamily = f),
        titleLarge = d.titleLarge.copy(fontFamily = f),
        titleMedium = d.titleMedium.copy(fontFamily = f),
        titleSmall = d.titleSmall.copy(fontFamily = f),
        bodyLarge = d.bodyLarge.copy(fontFamily = f, fontSize = 15.sp, lineHeight = 22.sp),
        bodyMedium = d.bodyMedium.copy(fontFamily = f),
        bodySmall = d.bodySmall.copy(fontFamily = f),
        labelLarge = d.labelLarge.copy(fontFamily = f),
        labelMedium = d.labelMedium.copy(fontFamily = f),
        labelSmall = d.labelSmall.copy(fontFamily = f),
    )
}
