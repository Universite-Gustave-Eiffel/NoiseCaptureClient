package org.noiseplanet.noisecapture.shared.signal

import kotlin.math.log10
import kotlin.math.pow


const val FAST_DECAY_RATE = -34.7
const val SLOW_DECAY_RATE = -4.3

/**
 * IEC 61672-1 standard for displayed sound level decay
 */
class LevelDisplayWeightedDecay(decibelDecayPerSecond: Double, newValueTimeInterval: Double) {

    val timeWeight = 10.0.pow(decibelDecayPerSecond * newValueTimeInterval / 10.0)
    var timeIntegration = 0.0

    fun getWeightedValue(newValue: Double): Double {
        timeIntegration =
            timeIntegration * timeWeight + 10.0.pow(newValue / 10.0) * (1 - timeWeight)
        return 10 * log10(timeIntegration)
    }

}
