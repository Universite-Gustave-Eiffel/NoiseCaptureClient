package org.noiseplanet.noisecapture.services.liveaudio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.audio.AcousticIndicatorsData
import org.noiseplanet.noisecapture.audio.AcousticIndicatorsProcessing
import org.noiseplanet.noisecapture.audio.AudioSource
import org.noiseplanet.noisecapture.audio.AudioSourceState
import org.noiseplanet.noisecapture.audio.WINDOW_TIME
import org.noiseplanet.noisecapture.audio.signal.FAST_DECAY_RATE
import org.noiseplanet.noisecapture.audio.signal.LevelDisplayWeightedDecay
import org.noiseplanet.noisecapture.audio.signal.window.SpectrumData
import org.noiseplanet.noisecapture.audio.signal.window.SpectrumDataProcessing
import org.noiseplanet.noisecapture.log.Logger

/**
 * Default [LiveAudioService] implementation.
 * Can be overridden in platforms to add specific behaviour.
 */
class DefaultLiveAudioService(
    private val audioSource: AudioSource,
) : LiveAudioService, KoinComponent {

    // - Constants

    companion object {

        private const val TAG = "LiveAudioService"

        const val FFT_SIZE = 4096
        const val FFT_HOP = 2048

        private const val SPL_DECAY_RATE = FAST_DECAY_RATE
        private const val SPL_WINDOW_TIME = WINDOW_TIME
    }


    // - Properties

    private val logger: Logger by inject { parametersOf(TAG) }

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

    private val _isRunningFlow = MutableStateFlow(false)


    // - LiveAudioService

    override val isRunningFlow: StateFlow<Boolean> = _isRunningFlow.asStateFlow()
    override val isRunning: Boolean
        get() = _isRunningFlow.value

    override val audioSourceState: Flow<AudioSourceState> = audioSource.stateFlow

    override fun setupAudioSource(startWhenReady: Boolean) {
        logger.debug("start when ready: $startWhenReady")
        // Create a job that will process incoming audio samples in a background thread
        audioJob = coroutineScope.launch(Dispatchers.Default) {
            audioSource.audioSamples
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

        // Setup audio source
        audioSource.setup()
    }

    override fun releaseAudioSource() {
        // Cancel processing job
        coroutineScope.launch(Dispatchers.Default) {
            audioJob?.cancel()
        }
        // Release audio source
        audioSource.release()
        _isRunningFlow.tryEmit(false)
    }

    override fun startListening() {
        // Start audio source
        audioSource.start()
        _isRunningFlow.tryEmit(true)
    }

    override fun stopListening() {
        // Pause audio source
        audioSource.pause()
        _isRunningFlow.tryEmit(false)
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
