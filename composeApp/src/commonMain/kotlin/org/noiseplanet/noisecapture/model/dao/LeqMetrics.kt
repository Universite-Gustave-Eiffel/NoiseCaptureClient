package org.noiseplanet.noisecapture.model.dao

import kotlinx.serialization.Serializable


/**
 * Overall Leq metrics of a measurement.
 *
 * @param min Min Leq value over the total duration of the measurement.
 * @param average Average Leq value over the total duration of the measurement.
 * @param max Max Leq value over the total duration of the measurement.
 * @param recordsCount Number of records taken into account.
 */
@Serializable
data class LeqMetrics(
    val min: Double,
    val average: Double,
    val max: Double,
    val recordsCount: Long,
)
