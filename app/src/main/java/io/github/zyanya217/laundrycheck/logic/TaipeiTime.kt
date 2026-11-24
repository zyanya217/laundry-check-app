package io.github.zyanya217.laundrycheck.logic

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// Hour indices count hours since the epoch on the Taipei wall clock (UTC+8, no daylight saving),
// matching HourlyRow.idx from the server.

private val TAIPEI: ZoneId = ZoneId.of("Asia/Taipei")
private val CLOCK: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(TAIPEI)

fun taipeiNowIdx(now: Instant = Instant.now()): Int =
    (Math.floorDiv(now.toEpochMilli(), 3_600_000L) + 8).toInt()

fun wallHour(idx: Int): Int = Math.floorMod(idx, 24)

fun pad(n: Int): String = n.toString().padStart(2, '0')

fun hh(h: Int): String = "${pad(h)}:00"

/** `HH:mm` in Taipei for an ISO-8601 instant, or an empty string when it cannot be parsed. */
fun taipeiClock(iso: String): String =
    try {
        CLOCK.format(Instant.parse(iso))
    } catch (_: DateTimeParseException) {
        ""
    }
