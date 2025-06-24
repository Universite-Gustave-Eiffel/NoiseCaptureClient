package org.noiseplanet.noisecapture.model.dao

import kotlinx.serialization.Serializable

/**
 * Measurement summary metrics that are calculated once the measurement is over.
 *
 * @param la10 10% of the time, sound level was below this value.
 * @param la50 50% of the time, sound level was below this value.
 * @param la90 90% of the time, sound level was below this value.
 */
@Serializable
data class MeasurementSummary(
    val la10: Double,
    val la50: Double,
    val la90: Double,
)
