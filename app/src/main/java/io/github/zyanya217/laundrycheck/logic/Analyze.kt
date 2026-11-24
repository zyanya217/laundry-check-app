package io.github.zyanya217.laundrycheck.logic

import io.github.zyanya217.laundrycheck.model.HourlyRow
import kotlin.math.exp
import kotlin.math.min

// Tunable constants. The dryness score is a heuristic, not a measurement: compare it with how your
// laundry really turns out and adjust these numbers. (Ported unchanged from the web app's analyze.ts.)
const val DRY_OK = 12.0 // score at or above this: dry by collect time
const val DRY_HALF = 6.0 // score at or above this: half dry
const val POP_MID = 30.0 // rain probability (%) that makes the verdict borderline
const val POP_BAD = 60.0 // rain probability (%) that makes it "dry indoors"
const val COVER_FACTOR = 0.5 // how much rain probability still counts when the balcony has a roof

enum class Level { OK, MID, BAD }

enum class Dryness { DRY, HALF, WET }

enum class Reason { OK_DRY, OK_HALF, RAIN_MID, RAIN_BAD, HUMID }

data class Analysis(
    val level: Level,
    val reason: Reason,
    val dry: Dryness,
    val score: Double,
    /** Taipei hour (0-23) when rain probability first reaches [POP_MID], or null when it never does. */
    val rainFrom: Int?,
    /** Taipei hour (0-23) after the last hour that reaches [POP_MID], or null when it never does. */
    val rainTo: Int?,
    val rainPop: Double,
)

private fun saturationKPa(t: Double): Double = 0.6108 * exp((17.27 * t) / (t + 237.3))

/** Drying power for one hour: vapour pressure deficit, boosted by wind, damped when rain is likely. */
fun evaporation(r: HourlyRow, covered: Boolean): Double {
    val vpd = saturationKPa(r.temp) * (1 - r.rh / 100)
    var e = vpd * (1 + 0.25 * min(r.wind, 6.0))
    if (r.pop >= 50) {
        e *= if (covered) 0.6 else 0.3
    } else if (r.pop >= POP_MID) {
        e *= if (covered) 0.85 else 0.6
    }
    return e
}

/** [rows] must not be empty; callers check for enough hours first. */
fun analyze(rows: List<HourlyRow>, covered: Boolean): Analysis {
    require(rows.isNotEmpty()) { "analyze needs at least one hour" }
    val cf = if (covered) COVER_FACTOR else 1.0
    val effPop = rows.maxOf { it.pop } * cf
    val rain = when {
        effPop >= POP_BAD -> Level.BAD
        effPop >= POP_MID -> Level.MID
        else -> Level.OK
    }

    val wet = rows.filter { it.pop >= POP_MID }
    val score = rows.sumOf { evaporation(it, covered) }
    val dry = when {
        score >= DRY_OK -> Dryness.DRY
        score >= DRY_HALF -> Dryness.HALF
        else -> Dryness.WET
    }

    var level = Level.OK
    var reason = if (dry == Dryness.DRY) Reason.OK_DRY else Reason.OK_HALF
    if (rain == Level.BAD) {
        level = Level.BAD
        reason = Reason.RAIN_BAD
    } else if (rain == Level.MID) {
        level = Level.MID
        reason = Reason.RAIN_MID
    } else if (dry == Dryness.WET) {
        level = Level.MID
        reason = Reason.HUMID
    }

    return Analysis(
        level = level,
        reason = reason,
        dry = dry,
        score = score,
        rainFrom = wet.firstOrNull()?.let { wallHour(it.idx) },
        rainTo = wet.lastOrNull()?.let { (wallHour(it.idx) + 1) % 24 },
        rainPop = wet.maxOfOrNull { it.pop } ?: 0.0,
    )
}
