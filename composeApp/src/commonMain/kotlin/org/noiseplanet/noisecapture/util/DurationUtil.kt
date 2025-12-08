package org.noiseplanet.noisecapture.util

import kotlin.math.abs
import kotlin.time.Duration


/**
 * Formats this duration to an "HH:MM:SS" string.
 *
 * @param hideHoursIfZero If true, the string will only include minutes and seconds if the number of
 *                        hours is zero. Defaults to false.
 *
 * @return "HH:MM:SS" or "MM:SS" format string.
 */
fun Duration.toHhMmSs(hideHoursIfZero: Boolean = false): String {
    return toComponents { hours, minutes, seconds, _ ->
        val minutesString = abs(minutes).toString().padStart(2, '0')
        val secondsString = abs(seconds).toString().padStart(2, '0')

        if (hideHoursIfZero && hours == 0L) {
            "$minutesString:$secondsString"
        } else {
            val hoursString = abs(hours).toString().padStart(2, '0')
            "$hoursString:$minutesString:$secondsString"
        }
    }
}
