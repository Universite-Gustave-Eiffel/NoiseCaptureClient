package org.noiseplanet.noisecapture.services.audio

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.noiseplanet.noisecapture.audio.AcousticIndicatorsData
import org.noiseplanet.noisecapture.audio.AudioSourceState
import org.noiseplanet.noisecapture.audio.signal.window.SpectrumData

/**
 * Listen to incoming audio and process samples to extract some acoustic indicators.
 */
interface LiveAudioService {

    /**
     * True if service is currently monitoring incoming audio,
     * false otherwise
     */
    val isRunning: Boolean

    /**
     * A flow of [isRunning] values.
     */
    val isRunningFlow: StateFlow<Boolean>

    /**
     * State of the underlying audio source.
     * Can be used to reflect system interruptions or resumes to user interface
     */
    val audioSourceState: AudioSourceState

    /**
     * A flow of [audioSourceState] values.
     */
    val audioSourceStateFlow: Flow<AudioSourceState>

    /**
     * Setup audio source for listening to incoming audio.
     */
    fun setupAudioSource()

    /**
     * Destroy audio source.
     */
    fun releaseAudioSource()

    /**
     * Starts listening to incoming audio samples through the provided audio source.
     * If a recording is already running, calling this again will have no effect.
     */
    fun startListening()

    /**
     * Stops listening to incoming audio samples through the provided audio source.
     * If no recording is running, this will have no effect.
     */
    fun stopListening()

    /**
     * Get a [Flow] of [AcousticIndicatorsData] from the currently running recording.
     */
    fun getAcousticIndicatorsFlow(): Flow<AcousticIndicatorsData>

    /**
     * Get a [Flow] of sound pressure level values.
     */
    fun getWeightedLeqFlow(): Flow<Double>

    /**
     * Get a [Flow] of sound pressure levels weighted by frequency band.
     */
    fun getWeightedSoundPressureLevelFlow(): Flow<Map<Int, Double>>

    /**
     * Get a [Flow] of [SpectrumData] from the currently running recording.
     */
    fun getSpectrumDataFlow(): Flow<SpectrumData>
}
