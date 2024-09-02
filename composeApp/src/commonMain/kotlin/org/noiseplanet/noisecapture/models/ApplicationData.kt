package org.noiseplanet.noisecapture.models

import kotlinx.serialization.Serializable

/**
 * Incremented when fields are added or modified
 * *note* add unit test for checking migration
 */
const val STORAGE_VERSION = 1

@Serializable
data class ApplicationData(
    val recordsUUID: MutableList<String> = mutableListOf(),
    val trafficCalibration: MutableList<TrafficCalibrationSession> = mutableListOf()
)
