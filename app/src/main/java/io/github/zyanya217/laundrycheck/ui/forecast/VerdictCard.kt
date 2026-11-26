package io.github.zyanya217.laundrycheck.ui.forecast

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.zyanya217.laundrycheck.R
import io.github.zyanya217.laundrycheck.logic.Analysis
import io.github.zyanya217.laundrycheck.logic.CollectDay
import io.github.zyanya217.laundrycheck.logic.DRY_HALF
import io.github.zyanya217.laundrycheck.logic.DRY_OK
import io.github.zyanya217.laundrycheck.logic.Dryness
import io.github.zyanya217.laundrycheck.logic.Reason
import io.github.zyanya217.laundrycheck.logic.hh
import io.github.zyanya217.laundrycheck.ui.components.ChevronDownIcon
import io.github.zyanya217.laundrycheck.ui.components.color
import io.github.zyanya217.laundrycheck.ui.components.outcomeRes
import io.github.zyanya217.laundrycheck.ui.components.res
import io.github.zyanya217.laundrycheck.ui.components.titleRes
import io.github.zyanya217.laundrycheck.ui.components.zoneRes
import io.github.zyanya217.laundrycheck.ui.theme.LaundryFonts
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private val START_HOURS = (17..23).toList()

// The meter runs from "still wet" to "dry"; its zone borders sit at the same score thresholds as the verdict.
private const val SCALE = DRY_OK * 1.4
private val ZONES = listOf(
    Dryness.WET to (DRY_HALF / SCALE).toFloat(),
    Dryness.HALF to ((DRY_OK - DRY_HALF) / SCALE).toFloat(),
    Dryness.DRY to ((SCALE - DRY_OK) / SCALE).toFloat(),
)

@Composable
fun VerdictCard(
    analysis: Analysis,
    dateLabel: String,
    collectDay: CollectDay,
    startHour: Int,
    collectHour: Int,
    startNote: String?,
    estimated: Boolean,
    onStartHour: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val levelColor = analysis.level.color()
    val from = analysis.rainFrom?.let(::hh).orEmpty()
    val to = analysis.rainTo?.let(::hh).orEmpty()

    Column(modifier) {
        Text(
            dateLabel,
            color = muted,
            fontFamily = LaundryFonts.Mono,
            fontSize = 12.sp,
            letterSpacing = 0.04.em,
        )
        Text(
            stringResource(analysis.level.titleRes()),
            modifier = Modifier.padding(top = 2.dp),
            color = levelColor,
            fontFamily = LaundryFonts.Display,
            fontSize = 32.sp,
            lineHeight = 40.sp,
        )
        Text(
            stringResource(
                if (estimated) when (analysis.reason) {
                    Reason.OK_DRY -> R.string.estimated_ok_dry
                    Reason.OK_HALF -> R.string.estimated_ok_half
                    Reason.RAIN_MID -> R.string.estimated_rain_mid
                    Reason.RAIN_BAD -> R.string.estimated_rain_bad
                    Reason.HUMID -> R.string.estimated_humid
                } else analysis.reason.res(),
                from, to, analysis.rainPop.roundToInt(),
            ),
            modifier = Modifier.padding(top = 8.dp),
            fontSize = 16.sp,
        )

        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StartHourPill(startHour, onStartHour)
            Canvas(Modifier.weight(1f).height(2.dp)) {
                drawLine(
                    color = muted.copy(alpha = 0.6f),
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                Text(stringResource(R.string.collect_label), color = muted, fontSize = 12.sp)
                Text(
                    "${stringResource(collectDay.res())} ${hh(collectHour)}",
                    fontFamily = LaundryFonts.Mono,
                    fontWeight = FontWeight.Medium,
                    fontSize = 17.sp,
                    maxLines = 1,
                )
            }
        }
        if (startNote != null) {
            Text(startNote, modifier = Modifier.padding(top = 4.dp), color = muted, fontSize = 12.sp)
        }

        Text(
            stringResource(R.string.meter_label),
            modifier = Modifier.padding(top = 10.dp),
            color = muted,
            fontSize = 12.sp,
        )
        DrynessMeter(analysis)
    }
}

@Composable
private fun StartHourPill(startHour: Int, onStartHour: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        onClick = { expanded = true },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.heightIn(min = 44.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.start_label), color = muted, fontSize = 12.sp)
            Text(hh(startHour), fontFamily = LaundryFonts.Mono, fontWeight = FontWeight.Medium, fontSize = 17.sp)
            ChevronDownIcon(color = MaterialTheme.colorScheme.onSurface)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            START_HOURS.forEach { h ->
                DropdownMenuItem(
                    text = { Text(hh(h), fontFamily = LaundryFonts.Mono) },
                    onClick = {
                        onStartHour(h)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun DrynessMeter(analysis: Analysis) {
    val track = MaterialTheme.colorScheme.surfaceContainerHigh
    val gap = MaterialTheme.colorScheme.background
    val fillColor = analysis.dry.color()
    val fill = max(6.0, min(100.0, analysis.score / SCALE * 100)).toFloat() / 100f
    val description = "${stringResource(R.string.meter_label)}: ${stringResource(analysis.dry.outcomeRes())}"

    Canvas(
        Modifier
            .padding(top = 6.dp)
            .fillMaxWidth()
            .height(16.dp)
            .semantics { contentDescription = description },
    ) {
        val round = CornerRadius(size.height / 2)
        drawRoundRect(track, size = size, cornerRadius = round)
        drawRoundRect(fillColor, size = Size(size.width * fill, size.height), cornerRadius = round)
        // Thin gaps mark where "still wet" turns into "half dry" and into "dry".
        var x = 0f
        ZONES.dropLast(1).forEach { (_, width) ->
            x += width
            drawRect(gap, topLeft = Offset(size.width * x, 0f), size = Size(3.dp.toPx(), size.height))
        }
    }
    Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
        ZONES.forEach { (dryness, width) ->
            val current = dryness == analysis.dry
            Text(
                stringResource(dryness.zoneRes()),
                modifier = Modifier.weight(width),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = if (current) FontWeight.Bold else FontWeight.Normal,
                color = if (current) dryness.color() else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
