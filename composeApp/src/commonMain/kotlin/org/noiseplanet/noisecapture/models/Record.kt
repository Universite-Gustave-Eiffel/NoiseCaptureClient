package org.noiseplanet.noisecapture.models

import kotlinx.serialization.Serializable

@Serializable
data class Record (
    val recordUtc : Long, // start of measurement, millisecond utc
    val userUUID: String, // random UUID set at application installation
    var versionNumber: Int, // Application build/version number
    var versionName: String, // Application version name/release
    var buildDate: String, //Compilation build date
    var deviceManufacturer: String, // Recording device manufacturer if available
    var userProfile: String, // User defined expertise at startup (novice,expert..)
    var deviceModel: String, // Recording device model if available
    var deviceProduct: String, // Recording device product if available
    var uploadId : String, // server side record unique identification
    var duration: Double, // recording duration in seconds
    var leqSequenceCount: Int, // number of indicators files
    var description: String,
    var photoUri: String,
    var pleasantness : Int,
    var gain: Double, // gain setting (deviation from platform sensitivity)
    var sensitivity: Double, // applied sensitivity used in dBFS to dB conversion,
    var noisePartyTag: String,
    var calibrationMethod: Int,
    var microphoneDeviceId: String,
    var microphoneDeviceSettings : String,
    var tags: MutableList<String>,
    var indicators: AggregatedIndicators
)
