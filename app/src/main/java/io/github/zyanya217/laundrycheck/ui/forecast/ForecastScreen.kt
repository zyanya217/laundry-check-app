package io.github.zyanya217.laundrycheck.ui.forecast

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zyanya217.laundrycheck.R
import io.github.zyanya217.laundrycheck.logic.ForecastContent
import io.github.zyanya217.laundrycheck.logic.hh
import io.github.zyanya217.laundrycheck.logic.wallHour
import io.github.zyanya217.laundrycheck.ui.components.SlidersIcon
import io.github.zyanya217.laundrycheck.ui.components.monthDay
import io.github.zyanya217.laundrycheck.ui.components.weekdayShort
import io.github.zyanya217.laundrycheck.ui.theme.LaundryFonts
import io.github.zyanya217.laundrycheck.ui.theme.laundry
import kotlin.math.roundToInt

@Composable
fun ForecastScreen(
    state: ForecastUiState,
    onOpenSettings: () -> Unit,
    onReload: () -> Unit,
    onStartHour: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val loading = state.status == FetchStatus.LOADING
    // Only content with a verdict can be shown; otherwise it is "loading" or "not enough data".
    val shown = state.content?.takeIf { it.analysis != null }

    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Header("${state.settings.county} ${state.settings.district}", onOpenSettings)

            when {
                state.status == FetchStatus.ERROR -> ErrorCard(state.errorCode, onReload)
                loading && shown == null -> SkeletonCard()
                shown == null -> NoDataCard()
                else -> Column(
                    modifier = Modifier.alpha(if (loading) 0.55f else 1f),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    ForecastBody(state, shown, onStartHour)
                }
            }

            Footer(loading = loading, updated = state.updatedClock)
        }
    }
}

@Composable
private fun ForecastBody(state: ForecastUiState, content: ForecastContent, onStartHour: (Int) -> Unit) {
    val window = content.window
    val analysis = checkNotNull(content.analysis)
    val locale = LocalConfiguration.current.locales[0]

    // "Tonight 11/20 Fri": the date of the evening being judged.
    val dayNumber = Math.floorDiv(window.startIdx, 24)
    val dateLabel = "${stringResource(R.string.tonight)} ${monthDay(dayNumber)} ${weekdayShort(dayNumber, locale)}"
    val startNote = if (window.startMoved) stringResource(R.string.from_note, hh(wallHour(window.startIdx))) else null
    val chartLabel = if (window.rows.size < 2) {
        ""
    } else {
        stringResource(
            R.string.chart_aria,
            hh(wallHour(window.rows.first().idx)),
            hh(wallHour(window.rows.last().idx)),
            window.rows.maxOf { it.pop }.roundToInt(),
        )
    }

    VerdictCard(
        analysis = analysis,
        dateLabel = dateLabel,
        collectDay = window.collectDay,
        startHour = state.settings.startHour,
        collectHour = state.settings.collectHour,
        startNote = startNote,
        onStartHour = onStartHour,
    )
    if (content.hasWeek) {
        HorizontalDivider()
        BestDays(picks = content.picks, best = content.bestDay)
    }
    HorizontalDivider()
    HourlyChart(rows = window.rows, label = chartLabel)
}

@Composable
private fun Header(place: String, onOpenSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.weight(1f, fill = false),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp).height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(place, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
            }
        }
        val description = stringResource(R.string.aria_set)
        IconButton(onClick = onOpenSettings, modifier = Modifier.semantics { contentDescription = description }) {
            SlidersIcon(color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun ErrorCard(code: String, onReload: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.laundry.badBg)
            .padding(20.dp)
            .semantics { liveRegion = LiveRegionMode.Assertive },
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(stringResource(R.string.load_err), color = MaterialTheme.laundry.bad, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.err_hint), fontSize = 15.sp)
        Text(code, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = LaundryFonts.Mono, fontSize = 12.sp)
        Button(onClick = onReload) { Text(stringResource(R.string.retry), fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun SkeletonCard() {
    val pulse by rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 900), RepeatMode.Reverse),
        label = "skeleton-alpha",
    )
    Box(
        Modifier
            .fillMaxWidth()
            .height(190.dp)
            .alpha(pulse)
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    )
}

@Composable
private fun NoDataCard() {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.laundry.midBg)
            .padding(20.dp),
    ) {
        Text(stringResource(R.string.no_data), color = MaterialTheme.laundry.mid, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Footer(loading: Boolean, updated: String) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(stringResource(R.string.src_cwa), color = muted, fontSize = 12.sp)
        val status = when {
            loading -> stringResource(R.string.loading)
            updated.isNotEmpty() -> stringResource(R.string.upd, updated)
            else -> ""
        }
        Text(status, color = muted, fontSize = 12.sp)
    }
}
