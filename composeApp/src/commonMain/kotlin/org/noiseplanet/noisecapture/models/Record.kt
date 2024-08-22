package org.noiseplanet.noisecapture.models

import kotlinx.serialization.Serializable

@Serializable
data class Record (
    val recordUtc : Long, // start of measurement, millisecond utc
    val userUUID: String, // random UUID set at application installation
    val versionNumber: Int, // Application build/version number
    val versionName: String, // Application version name/release
    val buildDate: String, //Compilation build date
    val deviceManufacturer: String, // Recording device manufacturer if available
    val userProfile: String, // User defined expertise at startup (novice,expert..)
    val deviceModel: String, // Recording device model if available
    val deviceProduct: String, // Recording device product if available
    val uploadId : String, // server side record unique identification
    val duration: Double, // recording duration in seconds
    val leqSequenceCount: Int, // number of indicators files
    val description: String,
    val photoUri: String,
    val pleasantness : Int,
    val gain: Double, // gain setting (deviation from platform sensitivity)
    val sensitivity: Double, // applied sensitivity used in dBFS to dB conversion,
    val noisePartyTag: String,
    val calibrationMethod: Int,
    val microphoneDeviceId: String,
    val microphoneDeviceSettings : String,
    val tags: List<String>,
    val indicators: AggregatedIndicators
)
