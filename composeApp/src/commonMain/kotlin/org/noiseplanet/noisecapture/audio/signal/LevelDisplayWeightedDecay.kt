package org.noiseplanet.noisecapture.audio.signal

import kotlin.math.log10
import kotlin.math.pow


const val FAST_DECAY_RATE = -34.7
const val SLOW_DECAY_RATE = -4.3

/**
 * IEC 61672-1 standard for displayed sound level decay
 *
 * TODO: Document parameters
 */
class LevelDisplayWeightedDecay(
    decibelDecayPerSecond: Double,
    newValueTimeInterval: Double,
) {

    private val timeWeight = 10.0.pow(decibelDecayPerSecond * newValueTimeInterval / 10.0)
    private var timeIntegration = 0.0

    /**
     * TODO: add documentation
     */
    fun getWeightedValue(newValue: Double): Double {
        timeIntegration = timeIntegration * timeWeight +
            10.0.pow(newValue / 10.0) * (1 - timeWeight)
        return 10 * log10(timeIntegration)
    }

}
