package io.github.zyanya217.laundrycheck.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.github.zyanya217.laundrycheck.MainActivity
import io.github.zyanya217.laundrycheck.R
import io.github.zyanya217.laundrycheck.logic.Dryness
import io.github.zyanya217.laundrycheck.logic.Level
import io.github.zyanya217.laundrycheck.logic.hh
import io.github.zyanya217.laundrycheck.ui.components.monthDay
import io.github.zyanya217.laundrycheck.ui.components.outcomeRes
import io.github.zyanya217.laundrycheck.ui.components.titleRes
import io.github.zyanya217.laundrycheck.ui.components.weekdayShort
import io.github.zyanya217.laundrycheck.ui.components.zoneRes
import io.github.zyanya217.laundrycheck.ui.theme.DarkLaundryColors
import io.github.zyanya217.laundrycheck.ui.theme.LightLaundryColors
import java.util.Locale

/** The three widgets in the picker. Each has one fixed layout, sized for its cell count. */
enum class WidgetKind(val padding: Int) {
    /** 2x2 */
    SMALL(12),

    /** 4x2 */
    MEDIUM(14),

    /** 4x3 */
    LARGE(12),
}

abstract class LaundryWidgetBase(private val kind: WidgetKind) : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val source = WidgetEntryPoint.from(context).dataSource()
        var data = source.snapshot()
        // First placement: nothing cached yet, so fetch once instead of showing "no advice".
        if (data is WidgetData.NoData && source.refresh()) data = source.snapshot()

        provideContent {
            GlanceTheme {
                WidgetContent(data, kind)
            }
        }
    }
}

class SmallWidget : LaundryWidgetBase(WidgetKind.SMALL)

class MediumWidget : LaundryWidgetBase(WidgetKind.MEDIUM)

class LargeWidget : LaundryWidgetBase(WidgetKind.LARGE)

abstract class LaundryWidgetReceiverBase : GlanceAppWidgetReceiver() {
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetRefresh.schedule(context)
        WidgetRefresh.refreshNow(context)
    }
}

class SmallWidgetReceiver : LaundryWidgetReceiverBase() {
    override val glanceAppWidget: GlanceAppWidget = SmallWidget()
}

class MediumWidgetReceiver : LaundryWidgetReceiverBase() {
    override val glanceAppWidget: GlanceAppWidget = MediumWidget()
}

class LargeWidgetReceiver : LaundryWidgetReceiverBase() {
    override val glanceAppWidget: GlanceAppWidget = LargeWidget()
}

// ---------------------------------------------------------------------------------------------
// Colours. The meaning colours are the app's fixed ones; surfaces and text follow the wallpaper.

private fun fixed(light: Color, dark: Color) = ColorProvider(day = light, night = dark)

private fun Level.fg() = when (this) {
    Level.OK -> fixed(LightLaundryColors.ok, DarkLaundryColors.ok)
    Level.MID -> fixed(LightLaundryColors.mid, DarkLaundryColors.mid)
    Level.BAD -> fixed(LightLaundryColors.bad, DarkLaundryColors.bad)
}

private fun Level.bg() = when (this) {
    Level.OK -> fixed(LightLaundryColors.okBg, DarkLaundryColors.okBg)
    Level.MID -> fixed(LightLaundryColors.midBg, DarkLaundryColors.midBg)
    Level.BAD -> fixed(LightLaundryColors.badBg, DarkLaundryColors.badBg)
}

private fun Level.shirt() = when (this) {
    Level.OK -> R.drawable.shirt_ok
    Level.MID -> R.drawable.shirt_mid
    Level.BAD -> R.drawable.shirt_bad
}

private val rainColor = fixed(LightLaundryColors.rain, DarkLaundryColors.rain)
private val bestTile = fixed(Color.White.copy(alpha = 0.7f), Color.White.copy(alpha = 0.12f))
private val track = fixed(Color.White.copy(alpha = 0.75f), Color.White.copy(alpha = 0.14f))

private fun style(
    size: Int,
    color: ColorProvider,
    bold: Boolean = false,
    center: Boolean = false,
    end: Boolean = false,
) = TextStyle(
    color = color,
    fontSize = size.sp,
    fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
    textAlign = when {
        center -> TextAlign.Center
        end -> TextAlign.End
        else -> TextAlign.Start
    },
)

// ---------------------------------------------------------------------------------------------

@Composable
private fun WidgetContent(data: WidgetData, kind: WidgetKind) {
    val context = LocalContext.current
    val background = when (data) {
        is WidgetData.Ready -> data.level.bg()
        WidgetData.NoData -> GlanceTheme.colors.surfaceVariant
    }
    val root = GlanceModifier
        .fillMaxSize()
        .background(background)
        .cornerRadius(android.R.dimen.system_app_widget_background_radius)
        .appWidgetBackground()
        .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
        .padding(kind.padding.dp)

    Box(root) {
        when (data) {
            WidgetData.NoData -> NoDataLayout()
            is WidgetData.Ready -> when (kind) {
                WidgetKind.SMALL -> SmallLayout(data)
                WidgetKind.MEDIUM -> MediumLayout(data)
                WidgetKind.LARGE -> LargeLayout(data)
            }
        }
    }
}

@Composable
private fun NoDataLayout() {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            provider = ImageProvider(R.drawable.shirt_none),
            contentDescription = null,
            modifier = GlanceModifier.width(40.dp).height(37.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(GlanceModifier.height(6.dp))
        Text(
            context.getString(R.string.no_data),
            style = style(13, GlanceTheme.colors.onSurfaceVariant, center = true),
            maxLines = 2,
        )
    }
}

/** 2x2: the shirt, the verdict and tomorrow morning's dryness. */
@Composable
private fun SmallLayout(d: WidgetData.Ready) {
    val context = LocalContext.current
    Column(GlanceModifier.fillMaxSize()) {
        Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Text(
                context.getString(R.string.tonight),
                modifier = GlanceModifier.defaultWeight(),
                style = style(12, GlanceTheme.colors.onSurfaceVariant),
            )
            Shirt(d.level, 36)
        }
        Spacer(GlanceModifier.defaultWeight())
        Text(
            context.getString(d.level.titleRes()),
            style = style(16, d.level.fg(), bold = true),
            maxLines = 1,
        )
        Text(
            context.getString(R.string.widget_morning, context.getString(d.dry.outcomeRes())),
            style = style(13, GlanceTheme.colors.onSurface),
            maxLines = 1,
        )
    }
}

/** 4x2: verdict and times on the left, the next evenings with their rain chance on the right. */
@Composable
private fun MediumLayout(d: WidgetData.Ready) {
    val context = LocalContext.current
    Row(GlanceModifier.fillMaxSize()) {
        Column(GlanceModifier.defaultWeight().fillMaxHeight()) {
            Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Text(
                    context.getString(R.string.tonight),
                    modifier = GlanceModifier.defaultWeight(),
                    style = style(12, GlanceTheme.colors.onSurfaceVariant),
                )
                Shirt(d.level, 28)
            }
            Text(
                context.getString(d.level.titleRes()),
                style = style(17, d.level.fg(), bold = true),
                maxLines = 1,
            )
            Spacer(GlanceModifier.defaultWeight())
            Text(
                "${hh(d.startHour)} → ${hh(d.collectHour)}",
                style = style(12, GlanceTheme.colors.onSurface),
                maxLines = 1,
            )
            Text(
                context.getString(R.string.widget_morning, context.getString(d.dry.outcomeRes())),
                style = style(12, GlanceTheme.colors.onSurface),
                maxLines = 1,
            )
        }
        Spacer(GlanceModifier.width(10.dp))
        Box(GlanceModifier.width(1.dp).fillMaxHeight().background(GlanceTheme.colors.outline)) {}
        Spacer(GlanceModifier.width(6.dp))
        Box(GlanceModifier.defaultWeight().fillMaxHeight(), contentAlignment = Alignment.Center) {
            DayStrip(d.days, shirtSize = 28, showDry = false)
        }
    }
}

/** 4x3: adds the dryness meter and each evening's dryness. */
@Composable
private fun LargeLayout(d: WidgetData.Ready) {
    val context = LocalContext.current
    val locale = Locale.getDefault()
    val date = "${context.getString(R.string.tonight)} ${monthDay(d.dayNumber)} ${weekdayShort(d.dayNumber, locale)}"
    Column(GlanceModifier.fillMaxSize()) {
        Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Shirt(d.level, 36)
            Spacer(GlanceModifier.width(8.dp))
            Column(GlanceModifier.defaultWeight()) {
                Text(date, style = style(12, GlanceTheme.colors.onSurfaceVariant), maxLines = 1)
                Text(
                    context.getString(d.level.titleRes()),
                    style = style(17, d.level.fg(), bold = true),
                    maxLines = 1,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${hh(d.startHour)} → ${hh(d.collectHour)}",
                    style = style(12, GlanceTheme.colors.onSurface),
                    maxLines = 1,
                )
                if (d.updatedClock.isNotEmpty()) {
                    Text(
                        context.getString(R.string.upd, d.updatedClock),
                        style = style(11, GlanceTheme.colors.onSurfaceVariant),
                        maxLines = 1,
                    )
                }
            }
        }
        Spacer(GlanceModifier.height(8.dp))
        LinearProgressIndicator(
            progress = d.meterFill,
            modifier = GlanceModifier.fillMaxWidth().height(10.dp),
            color = d.level.fg(),
            backgroundColor = track,
        )
        Row(GlanceModifier.fillMaxWidth().padding(top = 2.dp)) {
            listOf(Dryness.WET, Dryness.HALF, Dryness.DRY).forEach { z ->
                Text(
                    context.getString(z.zoneRes()),
                    modifier = GlanceModifier.defaultWeight(),
                    style = style(11, if (z == d.dry) d.level.fg() else GlanceTheme.colors.onSurfaceVariant, bold = z == d.dry, center = true),
                    maxLines = 1,
                )
            }
        }
        Spacer(GlanceModifier.defaultWeight())
        DayStrip(d.days, shirtSize = 28, showDry = true)
    }
}

/** The next evenings, side by side and evenly spaced. The best one sits on a lighter tile. */
@Composable
private fun DayStrip(days: List<WidgetDay>, shirtSize: Int, showDry: Boolean) {
    val context = LocalContext.current
    val locale = Locale.getDefault()
    Row(GlanceModifier.fillMaxWidth()) {
        days.forEach { day ->
            val label = when (day.offset) {
                0 -> context.getString(R.string.today)
                1 -> context.getString(R.string.tomorrow)
                else -> weekdayShort(day.dayNumber, locale)
            }
            val tile = GlanceModifier.defaultWeight().padding(vertical = 4.dp).cornerRadius(16.dp)
            Column(
                modifier = if (day.best) tile.background(bestTile) else tile,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(label, style = style(12, GlanceTheme.colors.onSurface, center = true), maxLines = 1)
                Shirt(day.level, shirtSize)
                Text("${day.pop}%", style = style(11, rainColor, center = true), maxLines = 1)
                if (showDry) {
                    Text(
                        context.getString(day.dry.outcomeRes()),
                        style = style(12, day.level.fg(), bold = true, center = true),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun Shirt(level: Level, width: Int) {
    Image(
        provider = ImageProvider(level.shirt()),
        contentDescription = null,
        modifier = GlanceModifier.width(width.dp).height((width * 33 / 36).dp),
        contentScale = ContentScale.Fit,
    )
}
