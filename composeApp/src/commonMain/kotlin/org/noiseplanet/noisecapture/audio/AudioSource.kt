package org.noiseplanet.noisecapture.audio

import kotlinx.coroutines.flow.Flow

/**
 * Common interface to access Audio samples from device microphone
 * As each device
 */
interface AudioSource {

    enum class MicrophoneLocation {
        LOCATION_UNKNOWN,
        LOCATION_MAIN_BODY,
        LOCATION_MAIN_BODY_MOVABLE,
        LOCATION_PERIPHERAL
    }

    /**
     * TODO: Improve documentation and methods naming
     * @param sampleRate Sample rate in Hz
     * @param bufferSize Buffer size in bytes
     * @return InitializeErrorCode instance
     */
    suspend fun setup(): Flow<AudioSamples>

    /**
     * Release device and will require to setup again before getting new samples
     * Will abort samples flow
     */
    fun release()

    /**
     * TODO: Document this
     */
    fun getMicrophoneLocation(): MicrophoneLocation
}
