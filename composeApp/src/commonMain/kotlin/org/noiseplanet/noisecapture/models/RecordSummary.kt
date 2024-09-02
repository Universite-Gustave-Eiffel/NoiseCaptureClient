package org.noiseplanet.noisecapture.models


/**
 * Short record info
 */
data class RecordSummary (
    val recordUUID: String, // resource key for the full record is using this UUID
    val recordUtc : Long, // start of measurement, millisecond utc
    var duration: Double, // recording duration in seconds
    val LA50: Double, // median noise level dB(A)
)