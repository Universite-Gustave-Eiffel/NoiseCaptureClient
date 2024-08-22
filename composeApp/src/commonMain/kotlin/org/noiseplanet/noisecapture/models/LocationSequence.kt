package org.noiseplanet.noisecapture.models

import kotlinx.serialization.Serializable

@Serializable
data class LocationSequence (
    val recordUtc : Long, // start of measurement, millisecond utc
    val userUUID: String, // random UUID set at application installation
    val locationSequence: List<LocationSequence>
)