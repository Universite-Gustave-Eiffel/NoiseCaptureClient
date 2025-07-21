package org.noiseplanet.noisecapture.services.audio

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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.audio.AcousticIndicatorsProcessing
import org.noiseplanet.noisecapture.audio.AudioSource
import org.noiseplanet.noisecapture.audio.AudioSourceState
import org.noiseplanet.noisecapture.audio.signal.LevelDisplayWeightedDecay
import org.noiseplanet.noisecapture.audio.signal.window.SpectrogramData
import org.noiseplanet.noisecapture.audio.signal.window.SpectrogramDataProcessing
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.dao.LeqRecord
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.services.permission.PermissionService
import org.noiseplanet.noisecapture.util.injectLogger
import kotlin.time.Duration

/**
 * Default [LiveAudioService] implementation.
 * Can be overridden in platforms to add specific behaviour.
 */
class DefaultLiveAudioService : LiveAudioService, KoinComponent {

    // - Constants

    companion object {

        const val FFT_SIZE = 4096
        const val FFT_HOP = 2048
    }


    // - Properties

    private val logger: Logger by injectLogger()
    private val audioSource: AudioSource by inject()
    private val permissionService: PermissionService by inject()

    private val isPermissionGrantedFlow: Flow<Boolean> = permissionService
        .getPermissionStateFlow(Permission.RECORD_AUDIO)
        .map { it == PermissionState.GRANTED }

    private var startOnReady: Boolean = false

    private var indicatorsProcessing: AcousticIndicatorsProcessing? = null
    private var spectrogramDataProcessing: SpectrogramDataProcessing? = null

    private var audioJob: Job? = null
    private val leqRecordsFlow = MutableSharedFlow<LeqRecord>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val spectrogramDataFlow = MutableSharedFlow<SpectrogramData>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _isRunningFlow = MutableStateFlow(false)


    // - LiveAudioService

    override val isRunningFlow: StateFlow<Boolean> = _isRunningFlow.asStateFlow()
    override val isRunning: Boolean
        get() = _isRunningFlow.value

    override val audioSourceState: AudioSourceState
        get() = audioSource.state
    override val audioSourceStateFlow: Flow<AudioSourceState>
        get() = audioSource.stateFlow


    override fun setupAudioSource() {
        // Create a job that will process incoming audio samples in a background thread
        audioJob = coroutineScope.launch {
            audioSource.audioSamples
                .collect { audioSamples ->
                    // Process acoustic indicators
                    if (indicatorsProcessing?.sampleRate != audioSamples.sampleRate) {
                        logger.debug("Processing audio indicators with sample rate of ${audioSamples.sampleRate}")
                        indicatorsProcessing = AcousticIndicatorsProcessing(audioSamples.sampleRate)
                    }
                    indicatorsProcessing?.processSamples(audioSamples)
                        ?.forEach {
                            leqRecordsFlow.tryEmit(it)
                        }

                    // Process spectrogram data
                    // TODO: Consider moving this to SpectrogramPlotViewModel so that FFT doesn't
                    //       always run in background when we don't need it.
                    if (spectrogramDataProcessing?.sampleRate != audioSamples.sampleRate) {
                        logger.debug("Processing spectrum data with sample rate of ${audioSamples.sampleRate}")
                        spectrogramDataProcessing = SpectrogramDataProcessing(
                            sampleRate = audioSamples.sampleRate,
                            windowSize = FFT_SIZE,
                            windowHop = FFT_HOP
                        )
                    }
                    spectrogramDataProcessing?.pushSamples(audioSamples.epoch, audioSamples.samples)
                        ?.forEach {
                            spectrogramDataFlow.tryEmit(it)
                        }
                }
        }

        // Setup audio source whenever microphone permission is granted.
        coroutineScope.launch {
            permissionService.getPermissionStateFlow(Permission.RECORD_AUDIO)
                .map { it == PermissionState.GRANTED }
                .collect { isPermissionGranted ->
                    if (isPermissionGranted) {
                        audioSource.setup()
                    }
                }
        }

        // Listen to audio source state to start it whenever it is ready
        coroutineScope.launch {
            audioSourceStateFlow.collect { state ->
                if (state == AudioSourceState.READY && startOnReady) {
                    audioSource.start()
                }
            }
        }
    }

    override fun releaseAudioSource() {
        // Cancel processing job
        audioJob?.cancel()
        // Release audio source
        audioSource.release()
        _isRunningFlow.tryEmit(false)
    }

    override fun startListening() {
        if (audioSourceState == AudioSourceState.UNINITIALIZED) {
            startOnReady = true
        } else {
            audioSource.start()
            _isRunningFlow.tryEmit(audioSourceState == AudioSourceState.RUNNING)
        }
    }

    override fun stopListening() {
        // Pause audio source, or cancel delayed start if needed
        audioSource.pause()
        startOnReady = false
        _isRunningFlow.tryEmit(false)
    }

    override fun getLeqRecordsFlow(): Flow<LeqRecord> {
        return leqRecordsFlow.asSharedFlow()
    }

    override fun getSpectrogramDataFlow(): Flow<SpectrogramData> {
        return spectrogramDataFlow.asSharedFlow()
    }

    override fun getWeightedLeqFlow(
        splDecayRate: Double,
        windowTime: Duration,
    ): Flow<Double> {
        val levelDisplay = LevelDisplayWeightedDecay(splDecayRate, windowTime)

        return getLeqRecordsFlow()
            .map {
                levelDisplay.getWeightedValue(it.laeq)
            }
    }

    override fun getWeightedLeqPerFrequencyBandFlow(
        splDecayRate: Double,
        windowTime: Duration,
    ): Flow<Map<Int, Double>> {
        var levelDisplayBands: Map<Int, LevelDisplayWeightedDecay>? = null

        return getLeqRecordsFlow()
            .map { indicators ->
                if (levelDisplayBands == null) {
                    levelDisplayBands = indicators.leqsPerThirdOctave.mapValues {
                        LevelDisplayWeightedDecay(splDecayRate, windowTime)
                    }
                }
                indicators.leqsPerThirdOctave.mapValues { entry ->
                    levelDisplayBands[entry.key]
                        ?.getWeightedValue(entry.value)
                        ?: 0.0
                }
            }
    }
}
