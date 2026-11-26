package io.github.zyanya217.laundrycheck.ui.forecast

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.zyanya217.laundrycheck.R
import io.github.zyanya217.laundrycheck.logic.HangWindow
import io.github.zyanya217.laundrycheck.logic.pad
import io.github.zyanya217.laundrycheck.logic.wallHour
import io.github.zyanya217.laundrycheck.ui.theme.LaundryFonts
import io.github.zyanya217.laundrycheck.ui.theme.laundry
import kotlin.math.min

// Drawing area inside the web app's 360 x 92 viewBox: the plot, then the hour labels.
private const val VIEW_W = 360f
private const val PLOT_L = 26f
private const val PLOT_R = 4f
private const val PLOT_TOP = 10f
private const val PLOT_BOT = 70f

private fun y(v: Float) = PLOT_BOT - (PLOT_BOT - PLOT_TOP) * v / 100f

@Composable
fun HourlyChart(window: HangWindow, label: String, modifier: Modifier = Modifier) {
    val ink = MaterialTheme.colorScheme.onSurface
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val line = MaterialTheme.colorScheme.outlineVariant
    val mid = MaterialTheme.laundry.mid
    val measurer = rememberTextMeasurer()

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.chart_title),
                color = muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.06.em,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Legend(stringResource(R.string.lg_rain), muted) {
                    Box(Modifier.size(width = 6.dp, height = 10.dp).background(ink, RoundedCornerShape(3.dp)))
                }
                Legend(stringResource(R.string.lg_rh), muted) {
                    Canvas(Modifier.width(14.dp).height(2.dp)) {
                        drawLine(
                            mid,
                            Offset(0f, size.height / 2),
                            Offset(size.width, size.height / 2),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.dp.toPx())),
                        )
                    }
                }
            }
        }

        Canvas(
            Modifier
                .fillMaxWidth()
                .aspectRatio(VIEW_W / 92f)
                .semantics { if (label.isNotEmpty()) contentDescription = label },
        ) {
            val slots = window.hourlySlots()
            val k = size.width / VIEW_W
            val step = (VIEW_W - PLOT_L - PLOT_R) / slots.size
            fun x(i: Int) = (PLOT_L + step * (i + 0.5f)) * k
            fun labelStyle(color: Color, weight: FontWeight = FontWeight.Normal) =
                TextStyle(fontFamily = LaundryFonts.Mono, fontSize = (10f * k).toSp(), color = color, fontWeight = weight)

            // Rain probability scale: 0 and 100 gridlines.
            listOf(0, 100).forEach { v ->
                val gy = y(v.toFloat()) * k
                drawLine(line, Offset(PLOT_L * k, gy), Offset((VIEW_W - PLOT_R) * k, gy), strokeWidth = 1f * k)
                val text = measurer.measure(v.toString(), labelStyle(muted))
                drawText(text, topLeft = Offset((PLOT_L - 6f) * k - text.size.width, gy - text.size.height / 2f))
            }

            slots.forEachIndexed { i, r ->
                val cx = x(i)
                val hour = wallHour(window.startIdx + i)
                val last = i == slots.lastIndex

                // A dashed rule where a new day starts.
                if (i > 0 && hour == 0) {
                    val mx = cx - step * k / 2f
                    drawLine(
                        muted.copy(alpha = 0.6f),
                        Offset(mx, PLOT_TOP * k),
                        Offset(mx, (PLOT_BOT + 4f) * k),
                        strokeWidth = 1f * k,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f * k, 3f * k)),
                    )
                }
                if (r != null) {
                    val barH = ((PLOT_BOT - PLOT_TOP) * r.pop.toFloat() / 100f) * k
                    if (barH > 0f) {
                        drawRoundRect(ink, Offset(cx - 3f * k, PLOT_BOT * k - barH), Size(6f * k, barH), CornerRadius(3f * k))
                    }
                }
                if (last || step >= 24f || i % 2 == 0) {
                    val text = measurer.measure(
                        pad(hour),
                        labelStyle(if (last) ink else muted, if (last) FontWeight.Medium else FontWeight.Normal),
                    )
                    drawText(text, topLeft = Offset(cx - text.size.width / 2f, (PLOT_BOT + 11f) * k - text.size.height / 2f))
                }
            }

            // Humidity as a dashed line over the bars.
            val humidity = Path()
            slots.forEachIndexed { i, r ->
                if (r == null) return@forEachIndexed
                val px = x(i)
                val py = y(min(100f, r.rh.toFloat())) * k
                if (i == 0 || slots[i - 1] == null) humidity.moveTo(px, py) else humidity.lineTo(px, py)
                if ((i == 0 || slots[i - 1] == null) && (i == slots.lastIndex || slots[i + 1] == null)) {
                    drawCircle(mid, radius = 1.5f * k, center = Offset(px, py))
                }
            }
            drawPath(
                humidity,
                mid,
                style = Stroke(
                    width = 1.6f * k,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f * k, 3f * k)),
                ),
            )
        }
    }
}

@Composable
private fun Legend(text: String, color: Color, swatch: @Composable () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        swatch()
        Text(text, color = color, fontSize = 12.sp)
    }
}
