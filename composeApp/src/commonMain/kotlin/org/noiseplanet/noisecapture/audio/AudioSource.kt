package org.noiseplanet.noisecapture.audio

import kotlinx.coroutines.flow.Flow
import org.noiseplanet.noisecapture.model.MicrophoneLocation

/**
 * Common interface to access Audio samples from device microphone.
 * Each platform will supply its own implementation
 */
interface AudioSource {

    /**
     * A flow of audio samples that will be emitted by the audio source whenever it
     * is currently recording.
     */
    val audioSamples: Flow<AudioSamples>

    /**
     * A flow of audio source states
     */
    val stateFlow: Flow<AudioSourceState>

    /**
     * Current state of the audio source
     */
    var state: AudioSourceState

    /**
     * Initializes the required components to start recording audio.
     * To start recording, call [AudioSource.start]
     */
    fun setup()

    /**
     * Starts recording audio from an initialized audio source.
     * Should be called after [AudioSource.setup] or after [AudioSource.pause].
     */
    fun start()

    /**
     * Pauses recording, can be resumed afterwards by calling [AudioSource.start] again.
     * To stop recording and cleanup the underlying audio components, use [AudioSource.release].
     */
    fun pause()

    /**
     * Stops recording and cleans up underlying audio components.
     * To start recording again after releasing, one must call [AudioSource.setup] again before
     * [AudioSource.start]
     */
    fun release()

    /**
     * Fetches the location of the currently used microphone.
     * See [MicrophoneLocation] for more details about the possible cases.
     *
     * @return Currently in use microphone location
     */
    fun getMicrophoneLocation(): MicrophoneLocation
}

/**
 * Describes the current state of an [AudioSource] instance
 */
enum class AudioSourceState {

    /**
     * Audio source not initialized.
     * Must call [AudioSource.setup] before starting recording samples.
     */
    UNINITIALIZED,

    /**
     * Audio source initialized and ready to record.
     * Must call [AudioSource.start] to start recording samples.
     */
    READY,

    /**
     * Audio source currently recording incoming audio.
     */
    RUNNING,
}

/**
 * Thrown when user tries to start recording on an uninitialized audio source.
 */
object UninitializedException : IllegalStateException(
    "Audio source must be initialized before recording audio"
)
