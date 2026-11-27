package io.github.zyanya217.laundrycheck.notification

import io.github.zyanya217.laundrycheck.logic.Level
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShouldNotifyTest {
    private val day = 20_000
    private fun idx(hour: Int) = day * 24 + hour

    @Test fun notifiesWhenGoodAndHourReached() =
        assertTrue(shouldNotify(true, 20, idx(20), Level.OK, null))

    @Test fun laterThanChosenHourStillNotifies() =
        assertTrue(shouldNotify(true, 20, idx(23), Level.OK, day - 1))

    @Test fun waitsForChosenHour() =
        assertFalse(shouldNotify(true, 20, idx(19), Level.OK, null))

    @Test fun onlyOncePerDay() =
        assertFalse(shouldNotify(true, 20, idx(22), Level.OK, day))

    @Test fun silentWhenNotGood() {
        assertFalse(shouldNotify(true, 20, idx(21), Level.MID, null))
        assertFalse(shouldNotify(true, 20, idx(21), Level.BAD, null))
        assertFalse(shouldNotify(true, 20, idx(21), null, null))
    }

    @Test fun silentWhenSwitchedOff() =
        assertFalse(shouldNotify(false, 20, idx(21), Level.OK, null))
}
