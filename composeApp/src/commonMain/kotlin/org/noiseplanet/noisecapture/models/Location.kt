package org.noiseplanet.noisecapture.models

import kotlinx.serialization.Serializable

@Serializable
data class Location(val deviceUtc: Long, // location utc milliseconds (from device time)
                    val accuracy: Double,
                    val locationUtc: Long, // location utc milliseconds (from gps time)
                    val altitude: Double, // location altitude
                    val speed: Double, // device speed estimation
                    val bearing: Double, // device orientation estimation
)
