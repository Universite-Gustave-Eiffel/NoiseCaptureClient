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

    enum class InitializeErrorCode {
        INITIALIZE_OK, // TODO: Make error code optional rather than have and OK case
        INITIALIZE_WRONG_BUFFER_SIZE,
        INITIALIZE_SAMPLE_RATE_NOT_SUPPORTED,
        INITIALIZE_ALREADY_INITIALIZED,
        INITIALIZE_NO_MICROPHONE
    }

    /**
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

    fun getMicrophoneLocation(): MicrophoneLocation
}
