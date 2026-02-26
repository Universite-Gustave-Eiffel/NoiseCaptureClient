package org.noiseplanet.noisecapture.model.dao

import kotlinx.serialization.Serializable


/**
 * Current model version, used for potential migrations.
 */
val MicrophoneCalibrationProfile.Companion.VERSION: Int get() = 2


/**
 * Calibration info for a given microphone at a given date.
 *
 * @param calibrationTimestamp Date of the calibration
 * @param compensationGain Compensation gain to apply to the microphone
 * @param microphoneInfo Associated microphone properties
 * @param uuid A unique identifier for a given microphone info
 */
@Serializable
data class MicrophoneCalibrationProfile(
    val calibrationTimestamp: Long,
    val compensationGain: Double,
    val microphoneInfo: MicrophoneInfo,
    val uuid: String = microphoneInfo.calibrationProfileIdentifier(),
)
