package org.noiseplanet.noisecapture.signal

import org.noiseplanet.noisecapture.util.toHhMmSs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds


class DurationUtilTest {

    @Test
    fun testToHhMmSs() {
        // Test with a duration of less than one hour
        var duration = 1_234.seconds
        assertEquals(duration.toHhMmSs(), "00:20:34")
        assertEquals(duration.toHhMmSs(hideHoursIfZero = true), "20:34")

        // Test with a duration of more than one hour
        duration = 12_345.seconds
        assertEquals(duration.toHhMmSs(), "03:25:45")
        assertEquals(duration.toHhMmSs(hideHoursIfZero = true), "03:25:45")
    }
}
