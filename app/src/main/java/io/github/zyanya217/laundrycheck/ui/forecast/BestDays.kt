package io.github.zyanya217.laundrycheck.ui.forecast

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.zyanya217.laundrycheck.R
import io.github.zyanya217.laundrycheck.logic.DayPick
import io.github.zyanya217.laundrycheck.logic.Level
import io.github.zyanya217.laundrycheck.ui.components.color
import io.github.zyanya217.laundrycheck.ui.components.monthDay
import io.github.zyanya217.laundrycheck.ui.components.outcomeRes
import io.github.zyanya217.laundrycheck.ui.components.weekdayShort
import io.github.zyanya217.laundrycheck.ui.theme.LaundryFonts
import io.github.zyanya217.laundrycheck.ui.theme.laundry
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

// The shirt outline and the sag of the washing line are the web app's SVG, drawn on the same 350 x 74 grid.
private const val SHIRT = "M12 3 L4 8 L7 14 L11 12 L11 32 L29 32 L29 12 L33 14 L36 8 L28 3 C26 6 14 6 12 3 Z"
private const val W = 350f

private fun sag(x: Float): Float {
    val u = x / W
    return (1 - u).pow(2) * 10 + 2 * (1 - u) * u * 26 + u * u * 10
}

@Composable
fun BestDays(picks: List<DayPick>, best: Int?, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            stringResource(R.string.best_title),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.06.em,
        )
        WashingLine(picks)
        // The columns tuck 6 dp up under the line, as in the web layout.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val pull = 6.dp.roundToPx()
                    layout(placeable.width, placeable.height - pull) { placeable.place(0, -pull) }
                },
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            picks.forEach { pick ->
                DayColumn(pick, isBest = pick.offset == best, locale = locale, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun WashingLine(picks: List<DayPick>) {
    val shirt = remember { PathParser().parsePathString(SHIRT).toPath() }
    val ink = MaterialTheme.colorScheme.onSurface
    val unjudged = MaterialTheme.colorScheme.outlineVariant
    val colors = picks.map { it.analysis?.level?.color() ?: unjudged }
    val levels = picks.map { it.analysis?.level }

    Canvas(Modifier.fillMaxWidth().aspectRatio(350f / 74f)) {
        scale(size.width / W, pivot = Offset.Zero) {
            val rope = Path().apply {
                moveTo(0f, 10f)
                quadraticTo(175f, 26f, 350f, 10f)
            }
            drawPath(rope, ink, style = Stroke(width = 1.5f))

            picks.indices.forEach { i ->
                val cx = W * (2 * i + 1) / (2 * picks.size)
                val cy = sag(cx)
                translate(cx - 22f, cy + 5f) {
                    scale(1.1f, pivot = Offset.Zero) {
                        drawPath(shirt, colors[i])
                        levels[i]?.let { level -> drawFace(level) }
                    }
                }
                drawRoundRect(ink, Offset(cx - 12f, cy - 2f), Size(4f, 10f), CornerRadius(1.5f))
                drawRoundRect(ink, Offset(cx + 8f, cy - 2f), Size(4f, 10f), CornerRadius(1.5f))
            }
        }
    }
}

/** A smile, a flat mouth and a frown, so good and bad do not rely on colour alone. */
private fun DrawScope.drawFace(level: Level) {
    drawCircle(Color.White, radius = 1.3f, center = Offset(16f, 20f))
    drawCircle(Color.White, radius = 1.3f, center = Offset(24f, 20f))
    val mouth = Path().apply {
        when (level) {
            Level.OK -> { moveTo(16f, 25f); quadraticTo(20f, 29.5f, 24f, 25f) }
            Level.MID -> { moveTo(17f, 26.5f); lineTo(23f, 26.5f) }
            Level.BAD -> { moveTo(16f, 28.5f); quadraticTo(20f, 24f, 24f, 28.5f) }
        }
    }
    drawPath(mouth, Color.White, style = Stroke(width = 1.5f, cap = StrokeCap.Round))
}

@Composable
private fun DayColumn(pick: DayPick, isBest: Boolean, locale: Locale, modifier: Modifier) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val analysis = pick.analysis
    val name = when (pick.offset) {
        0 -> stringResource(R.string.today)
        1 -> stringResource(R.string.tomorrow)
        else -> weekdayShort(pick.dayNumber, locale)
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (isBest) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent)
            .padding(top = 8.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(monthDay(pick.dayNumber), color = muted, fontFamily = LaundryFonts.Mono, fontSize = 11.sp)
        if (analysis != null) {
            Text(
                stringResource(analysis.dry.outcomeRes()),
                modifier = Modifier.padding(top = 2.dp),
                color = analysis.level.color(),
                fontFamily = LaundryFonts.Display,
                fontSize = 16.sp,
            )
            Text(
                "${stringResource(R.string.best_rain)} ${pick.maxPop.roundToInt()}%",
                color = muted,
                fontFamily = LaundryFonts.Mono,
                fontSize = 12.sp,
            )
        } else {
            Text(stringResource(R.string.best_nodata), modifier = Modifier.padding(top = 4.dp), color = muted, fontSize = 13.sp)
        }
        Box(Modifier.padding(top = 4.dp).heightIn(min = 18.dp), contentAlignment = Alignment.Center) {
            if (isBest) {
                Text(
                    stringResource(if (analysis?.level == Level.BAD) R.string.best_relative else R.string.best_top),
                    modifier = Modifier
                        .background(if (analysis?.level == Level.BAD) MaterialTheme.laundry.bad else MaterialTheme.laundry.ok, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 2.dp),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 14.sp,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                )
            }
        }
    }
}
