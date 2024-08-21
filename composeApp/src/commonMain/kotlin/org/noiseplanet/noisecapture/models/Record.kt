package org.noiseplanet.noisecapture.models

import kotlinx.serialization.Serializable

@Serializable
data class Record (
    val recordUtc : Long,
    val uploadId : String,
    val leqMean: Double,
    val duration: Double, // recording duration in seconds
    val leqSequenceCount: Int, // number of indicators files
    val description: String,
    val photoUri: String,
    val pleasantness : Int,
    val gain: Double, // gain setting (deviation from platform sensitivity)
    val sensitivity: Double, // applied sensitivity in dBFS to dB conversion,
    val noisePartyTag: String,
    val calibrationMethod: Int,
    val microphoneDeviceId: String,
    val microphoneDeviceSettings : String,
    val tags: List<String>
)
