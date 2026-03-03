package org.noiseplanet.noisecapture.model.dao

import kotlinx.serialization.Serializable


/**
 * Current model version, used for potential migrations.
 */
val MicrophoneCalibrationProfile.Companion.VERSION: Int get() = 2


/**
 * Calibration profile of a given microphone.
 *
 * @param microphoneInfo Associated microphone properties.
 * @param isCalibrated True if microphone was calibrated, false otherwise.
 * @param compensationGain Compensation gain to apply to the microphone. Defaults to 0.0 if never calibrated.
 * @param calibrationTimestamp Date of the last calibration, if ever calibrated. Defaults to epoch.
 * @param uuid A unique identifier for a given microphone info.
 */
@Serializable
data class MicrophoneCalibrationProfile(
    val microphoneInfo: MicrophoneInfo,
    val compensationGain: Double = 0.0,
    val isCalibrated: Boolean = false,
    val calibrationTimestamp: Long = 0L,
    val uuid: String = microphoneInfo.calibrationProfileIdentifier(),
)
