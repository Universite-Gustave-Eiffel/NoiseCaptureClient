package org.noiseplanet.noisecapture.util

/**
 * Returns a string representing this string as a frequency value.
 * The unit (Hz or kHz) will be dynamically inferred from the int value.
 *
 * @return Frequency representation of self.
 */
fun Int.toFrequencyString(): String {
    return if (this >= 1000) {
        if (this % 1000 > 0) {
            val subKilo = (this % 1000).toString().trimEnd('0')
            "${this / 1000}.$subKilo kHz"
        } else {
            "${this / 1000} kHz"
        }
    } else {
        "$this Hz"
    }
}
