package io.github.zyanya217.laundrycheck.ui.components

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.github.zyanya217.laundrycheck.R
import io.github.zyanya217.laundrycheck.logic.CollectDay
import io.github.zyanya217.laundrycheck.logic.Dryness
import io.github.zyanya217.laundrycheck.logic.Level
import io.github.zyanya217.laundrycheck.logic.Reason
import io.github.zyanya217.laundrycheck.ui.theme.laundry
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@StringRes
fun Level.titleRes(): Int = when (this) {
    Level.OK -> R.string.v_ok
    Level.MID -> R.string.v_mid
    Level.BAD -> R.string.v_bad
}

@StringRes
fun Reason.res(): Int = when (this) {
    Reason.OK_DRY -> R.string.ok_dry
    Reason.OK_HALF -> R.string.ok_half
    Reason.RAIN_MID -> R.string.rain_mid
    Reason.RAIN_BAD -> R.string.rain_bad
    Reason.HUMID -> R.string.humid
}

/** "Dry", "Half dry", "Still wet": the outcome shown per evening and in the meter's accessibility text. */
@StringRes
fun Dryness.outcomeRes(): Int = when (this) {
    Dryness.DRY -> R.string.dry_dry
    Dryness.HALF -> R.string.dry_half
    Dryness.WET -> R.string.dry_wet
}

/** The short zone names under the meter. */
@StringRes
fun Dryness.zoneRes(): Int = when (this) {
    Dryness.DRY -> R.string.z_dry
    Dryness.HALF -> R.string.z_half
    Dryness.WET -> R.string.z_wet
}

@StringRes
fun CollectDay.res(): Int = when (this) {
    CollectDay.TODAY -> R.string.today
    CollectDay.TOMORROW -> R.string.tomorrow
    CollectDay.DAY_AFTER_TOMORROW -> R.string.dat
}

@Composable
fun Level.color(): Color = with(MaterialTheme.laundry) {
    when (this@color) {
        Level.OK -> ok
        Level.MID -> mid
        Level.BAD -> bad
    }
}

@Composable
fun Dryness.color(): Color = with(MaterialTheme.laundry) {
    when (this@color) {
        Dryness.DRY -> ok
        Dryness.HALF -> mid
        Dryness.WET -> bad
    }
}

// A day number counts wall-clock days since the epoch, so it maps straight to the Taipei calendar date.

fun monthDay(dayNumber: Int): String {
    val d = LocalDate.ofEpochDay(dayNumber.toLong())
    return "${d.monthValue}/${d.dayOfMonth}"
}

fun weekdayShort(dayNumber: Int, locale: Locale): String =
    LocalDate.ofEpochDay(dayNumber.toLong()).dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
