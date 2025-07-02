package org.noiseplanet.noisecapture.audio.signal

import kotlin.math.log10
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.DurationUnit


/**
 * IEC 61672-1 standard for displayed sound level decay
 *
 * @param decibelDecayPerSecond Decibels decay rate.
 * @param newValueTimeInterval Duration between each new sample.
 */
class LevelDisplayWeightedDecay(
    decibelDecayPerSecond: Double,
    newValueTimeInterval: Duration,
) {
    // - Constants

    companion object {

        const val FAST_DECAY_RATE = -34.7
        const val SLOW_DECAY_RATE = -4.3
    }


    // - Properties

    private val timeIntervalSeconds = newValueTimeInterval.toDouble(unit = DurationUnit.SECONDS)
    private val timeWeight = 10.0.pow(decibelDecayPerSecond * timeIntervalSeconds / 10.0)
    private var timeIntegration = 0.0


    // - Public functions

    /**
     * TODO: add documentation
     */
    fun getWeightedValue(newValue: Double): Double {
        timeIntegration = timeIntegration * timeWeight +
            10.0.pow(newValue / 10.0) * (1 - timeWeight)
        return 10 * log10(timeIntegration)
    }
}
