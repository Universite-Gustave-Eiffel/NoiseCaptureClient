package org.noiseplanet.noisecapture.util

import kotlin.math.pow
import kotlin.math.roundToInt


/**
 * Rounds this number to the Nth decimal place.
 *
 * Example:
 * ```kotlin
 * 10.467.roundTo(1) // -> 10.5
 * ```
 *
 * @param decimalPlaces Number of decimals to keep
 * @return Rounded number
 */
fun Double.roundTo(decimalPlaces: Int): Double {
    val factor = 10.0.pow(decimalPlaces)
    return (this * factor).roundToInt() / factor
}


/**
 * Rounds this number to the Nth decimal place.
 *
 * Example:
 * ```kotlin
 * 10.467.roundTo(1) // -> 10.5
 * ```
 *
 * @param decimalPlaces Number of decimals to keep
 * @return Rounded number
 */
fun Float.roundTo(decimalPlaces: Int): Float {
    val factor = 10.0f.pow(decimalPlaces)
    return (this * factor).roundToInt() / factor
}
