package org.noiseplanet.noisecapture.model.dao

import kotlinx.serialization.Serializable

/**
 * Current model version, used for potential migrations.
 */
val Measurement.Companion.VERSION: Int get() = 2

/**
 * Represents a measurement consisting of acoustic indicators recorded at different locations.
 *
 * @param uuid Unique measurement identifier.
 * @param startTimestamp Time at start of measurement, in milliseconds since epoch (UTC).
 * @param endTimestamp Time at end of measurement, in milliseconds since epoch (UTC).
 * @param duration Duration of measurement, in milliseconds.
 * @param userAgent Context of measurement (platform, device, app version)
 * @param locationSequenceIds Unique identifiers of location sequence fragments for this measurement.
 * @param leqsSequenceIds Unique identifiers of leq sequence fragments for this measurement.
 * @param recordedAudioUrl If audio recording is enabled, URL of the local audio recording.
 */
@Serializable
data class Measurement(
    val uuid: String,

    val startTimestamp: Long,
    val endTimestamp: Long,
    val duration: Long,

    val userAgent: UserAgent,

    val locationSequenceIds: List<String> = emptyList(),
    val leqsSequenceIds: List<String> = emptyList(),
    val laeqMetrics: LAeqMetrics,

    val summary: MeasurementSummary? = null,
    val recordedAudioUrl: String? = null,
)

/**
 * A mutable measurement, used to store information of an ongoing recording
 *
 * @param uuid Unique measurement identifier.
 * @param startTimestamp Time at start of measurement, in milliseconds since epoch (UTC).
 * @param endTimestamp Time at end of measurement, in milliseconds since epoch (UTC).
 *                     Set only when measurement ends.
 * @param locationSequenceIds Unique identifiers of location sequence fragments for this measurement.
 * @param leqsSequenceIds Unique identifiers of leq sequence fragments for this measurement.
 * @param recordedAudioUrl If audio recording is enabled, URL of the local audio recording.
 *                         Set only when measurement ends
 */
data class MutableMeasurement(
    val uuid: String,

    val startTimestamp: Long,
    var endTimestamp: Long? = null,

    val locationSequenceIds: MutableList<String> = mutableListOf(),
    val leqsSequenceIds: MutableList<String> = mutableListOf(),
    var laeqMetrics: LAeqMetrics? = null,

    var recordedAudioUrl: String? = null,
)
