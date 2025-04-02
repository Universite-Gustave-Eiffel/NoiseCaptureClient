package org.noiseplanet.noisecapture.model.measurement

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.noiseplanet.noisecapture.model.LocationRecord
import org.noiseplanet.noisecapture.model.UserAgent
import kotlin.time.Duration

/**
 * Represents a measurement consisting of acoustic indicators recorded at different locations
 */
@Serializable
data class Measurement(
    val uuid: String,

    val startedAt: Instant,
    val endedAt: Instant,
    val duration: Duration,

    val userAgent: UserAgent,

    val locationSequence: List<LocationRecord> = emptyList(),
    val leqsSequence: List<LeqsRecord> = emptyList(),

    val recordedAudioUrl: String? = null,
)

/**
 * A mutable measurement, used to store information of an ongoing recording
 */
data class MutableMeasurement(
    val startedAt: Instant,
    var endedAt: Instant? = null,

    val locationSequence: MutableList<LocationRecord> = mutableListOf(),
    val leqsSequence: MutableList<LeqsRecord> = mutableListOf(),

    var recordedAudioUrl: String? = null,
)
