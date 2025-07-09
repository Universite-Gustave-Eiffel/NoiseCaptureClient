package org.noiseplanet.noisecapture.services.audio

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.noiseplanet.noisecapture.audio.AcousticIndicatorsProcessing
import org.noiseplanet.noisecapture.audio.AudioSourceState
import org.noiseplanet.noisecapture.audio.signal.LevelDisplayWeightedDecay
import org.noiseplanet.noisecapture.audio.signal.window.SpectrumData
import org.noiseplanet.noisecapture.model.dao.LeqRecord
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Listen to incoming audio and process samples to extract some acoustic indicators.
 */
interface LiveAudioService {

    // - Constants

    companion object {

        protected const val DEFAULT_WEIGHTED_SPL_DECAY_RATE: Double =
            LevelDisplayWeightedDecay.FAST_DECAY_RATE

        protected val DEFAULT_WEIGHTED_SPL_WINDOW_TIME: Duration =
            AcousticIndicatorsProcessing.WINDOW_TIME_SECONDS.toDuration(
                unit = DurationUnit.SECONDS
            )
    }


    // - Properties

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


    // - Public functions

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
     * Get a [Flow] of [LeqRecord] from the currently running recording.
     */
    fun getLeqRecordsFlow(): Flow<LeqRecord>

    /**
     * Get a [Flow] of sound pressure level values.
     *
     * @param splDecayRate Decibels decay per second.
     * @param windowTime Time interval between samples.
     */
    fun getWeightedLeqFlow(
        splDecayRate: Double = DEFAULT_WEIGHTED_SPL_DECAY_RATE,
        windowTime: Duration = DEFAULT_WEIGHTED_SPL_WINDOW_TIME,
    ): Flow<Double>

    /**
     * Get a [Flow] of sound pressure levels weighted by frequency band.
     *
     * @param splDecayRate Decibels decay per second.
     * @param windowTime Time interval between samples.
     */
    fun getWeightedLeqPerFrequencyBandFlow(
        splDecayRate: Double = DEFAULT_WEIGHTED_SPL_DECAY_RATE,
        windowTime: Duration = DEFAULT_WEIGHTED_SPL_WINDOW_TIME,
    ): Flow<Map<Int, Double>>

    /**
     * Get a [Flow] of [SpectrumData] from the currently running recording.
     */
    fun getSpectrumDataFlow(): Flow<SpectrumData>
}
