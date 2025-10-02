package org.noiseplanet.noisecapture.util

import kotlin.math.log10
import kotlin.math.pow


/**
 * Returns a decibel average of the values in the collection.
 */
fun List<Double>.dbAverage(): Double {
    return 10.0 * log10(
        sumOf { 10.0.pow(it / 10.0) } / size
    )
}
