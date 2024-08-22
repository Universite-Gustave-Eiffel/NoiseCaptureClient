package org.noiseplanet.noisecapture.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.audio.AcousticIndicatorsData
import org.noiseplanet.noisecapture.audio.AcousticIndicatorsProcessing
import org.noiseplanet.noisecapture.audio.AudioSource
import org.noiseplanet.noisecapture.audio.WINDOW_TIME
import org.noiseplanet.noisecapture.audio.signal.FAST_DECAY_RATE
import org.noiseplanet.noisecapture.audio.signal.LevelDisplayWeightedDecay
import org.noiseplanet.noisecapture.audio.signal.window.SpectrumData
import org.noiseplanet.noisecapture.audio.signal.window.SpectrumDataProcessing
import org.noiseplanet.noisecapture.log.Logger

/**
 * Record, observe and save audio measurements.
 */
interface MeasurementsService {

    /**
     * Starts recording audio through the provided audio source.
     * If already a recording is already running, calling this again will have no effect.
     */
    fun startRecordingAudio()

    /**
     * Stops the currently running audio recording.
     * If no recording is running, this will have no effect
     */
    fun stopRecordingAudio()

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
    fun getWeightedSoundPressureLevelFlow(): Flow<DoubleArray>

    /**
     * Get a [Flow] of [SpectrumData] from the currently running recording.
     */
    fun getSpectrumDataFlow(): Flow<SpectrumData>
}

/**
 * Default [MeasurementsService] implementation.
 * Can be overridden in platforms to add specific behaviour.
 */
class DefaultMeasurementService(
    private val audioSource: AudioSource,
    private val logger: Logger,
) : MeasurementsService, KoinComponent {

    companion object {

        const val FFT_SIZE = 4096
        const val FFT_HOP = 2048

        private const val SPL_DECAY_RATE = FAST_DECAY_RATE
        private const val SPL_WINDOW_TIME = WINDOW_TIME
    }

    private var indicatorsProcessing: AcousticIndicatorsProcessing? = null
    private var spectrumDataProcessing: SpectrumDataProcessing? = null

    private var audioJob: Job? = null
    private val acousticIndicatorsFlow = MutableSharedFlow<AcousticIndicatorsData>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val spectrumDataFlow = MutableSharedFlow<SpectrumData>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun startRecordingAudio() {
        if (audioJob?.isActive == true) {
            logger.debug("Audio recording is already running. Don't start again.")
            return
        }
        logger.debug("Starting recording audio samples...")
        // Start recording and processing audio samples in a background thread
        audioJob = coroutineScope.launch(Dispatchers.Default) {
            audioSource.setup()
                .flowOn(Dispatchers.Default)
                .collect { audioSamples ->
                    // Process acoustic indicators
                    if (indicatorsProcessing?.sampleRate != audioSamples.sampleRate) {
                        logger.debug("Processing audio indicators with sample rate of ${audioSamples.sampleRate}")
                        indicatorsProcessing = AcousticIndicatorsProcessing(audioSamples.sampleRate)
                    }
                    indicatorsProcessing?.processSamples(audioSamples)
                        ?.forEach {
                            acousticIndicatorsFlow.tryEmit(it)
                        }

                    // Process spectrum data
                    if (spectrumDataProcessing?.sampleRate != audioSamples.sampleRate) {
                        logger.debug("Processing spectrum data with sample rate of ${audioSamples.sampleRate}")
                        spectrumDataProcessing = SpectrumDataProcessing(
                            sampleRate = audioSamples.sampleRate,
                            windowSize = FFT_SIZE,
                            windowHop = FFT_HOP
                        )
                    }
                    spectrumDataProcessing?.pushSamples(audioSamples.epoch, audioSamples.samples)
                        ?.forEach {
                            spectrumDataFlow.tryEmit(it)
                        }
                }
        }
    }

    override fun stopRecordingAudio() {
        coroutineScope.launch(Dispatchers.Default) {
            audioJob?.cancel()
            audioSource.release()
        }
    }

    override fun getAcousticIndicatorsFlow(): Flow<AcousticIndicatorsData> {
        return acousticIndicatorsFlow.asSharedFlow()
    }

    override fun getSpectrumDataFlow(): Flow<SpectrumData> {
        return spectrumDataFlow.asSharedFlow()
    }

    override fun getWeightedLeqFlow(): Flow<Double> {
        val levelDisplay = LevelDisplayWeightedDecay(SPL_DECAY_RATE, SPL_WINDOW_TIME)

        return getAcousticIndicatorsFlow()
            .map {
                levelDisplay.getWeightedValue(it.leq)
            }
    }

    override fun getWeightedSoundPressureLevelFlow(): Flow<DoubleArray> {
        var levelDisplayBands: Array<LevelDisplayWeightedDecay>? = null

        return getAcousticIndicatorsFlow()
            .map { indicators ->
                if (levelDisplayBands == null) {
                    levelDisplayBands = Array(indicators.nominalFrequencies.size) {
                        LevelDisplayWeightedDecay(SPL_DECAY_RATE, SPL_WINDOW_TIME)
                    }
                }
                DoubleArray(indicators.nominalFrequencies.size) { index ->
                    levelDisplayBands?.get(index)
                        ?.getWeightedValue(indicators.thirdOctave[index])
                        ?: 0.0
                }
            }
    }
}
