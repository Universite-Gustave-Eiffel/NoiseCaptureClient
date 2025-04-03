package org.noiseplanet.noisecapture.model.dao

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class LeqsRecord(
    val timestamp: Instant,

    val lzeq: Double,
    val laeq: Double,
    val lceq: Double,

    val leqsPerThirdOctaveBand: List<Double>,
)
