package org.noiseplanet.noisecapture.model.dao

import kotlinx.serialization.Serializable
import org.noiseplanet.noisecapture.audio.mic.MicrophoneType


/**
 * Current model version, used for potential migrations.
 */
val MicrophoneCalibrationProfile.Companion.VERSION: Int get() = 1


/**
 * Calibration info for a given microphone at a given date.
 *
 * @param calibrationTimestamp Date of the calibration
 * @param compensationGain Compensation gain to apply to the microphone
 * @param microphoneType Microphone type (builtin, aux, usb, ...)
 */
@Serializable
data class MicrophoneCalibrationProfile(
    val calibrationTimestamp: Long,
    val compensationGain: Double,
    val microphoneType: MicrophoneType,
)
