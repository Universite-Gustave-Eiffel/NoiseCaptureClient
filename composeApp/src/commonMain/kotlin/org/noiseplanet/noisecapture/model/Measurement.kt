package org.noiseplanet.noisecapture.model

import org.noiseplanet.noisecapture.audio.AcousticIndicatorsData
import kotlin.random.Random

/**
 * Represents a measurement consisting of acoustic indicators recorded at different locations
 */
data class Measurement(
    val id: Long = Random.nextLong(),
    val acousticIndicators: List<AcousticIndicatorsData> = emptyList(),
    val userLocationHistory: List<Location> = emptyList(),
    val recordedAudioUrl: String? = null,
)
