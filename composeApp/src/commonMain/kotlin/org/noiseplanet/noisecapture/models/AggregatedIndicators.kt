package org.noiseplanet.noisecapture.models

import kotlinx.serialization.Serializable

/**
 * Indicators displayed in app, it can be recomputed from LeqSequence data
 */
@Serializable
data class AggregatedIndicators (
    val periodInterval : Double, //time is seconds between each elements in periodLAeq
    val periodLAeq: FloatArray, // summary of lAeq for plot
    val LAMin: Double,
    val LA90: Double,
    val LA50: Double,
    val LA10: Double,
    val LAMax: Double
)