package org.noiseplanet.noisecapture.services.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.audio.AcousticIndicatorsProcessing
import org.noiseplanet.noisecapture.audio.AudioSamples
import org.noiseplanet.noisecapture.audio.AudioSource
import org.noiseplanet.noisecapture.audio.signal.LevelDisplayWeightedDecay
import org.noiseplanet.noisecapture.audio.signal.window.SpectrogramData
import org.noiseplanet.noisecapture.audio.signal.window.SpectrogramDataProcessing
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.dao.LeqRecord
import org.noiseplanet.noisecapture.model.enums.SpectrogramWindowingMode
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.services.permission.PermissionService
import org.noiseplanet.noisecapture.services.settings.SettingsKey
import org.noiseplanet.noisecapture.services.settings.UserSettingsService
import org.noiseplanet.noisecapture.util.injectLogger
import kotlin.time.Duration

/**
 * Default [LiveAudioService] implementation.
 * Can be overridden in platforms to add specific behaviour.
 */
@OptIn(ExperimentalCoroutinesApi::class)
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
    private val settingsService: UserSettingsService by inject()

    private var startOnReady: Boolean = false

    private var indicatorsProcessing: AcousticIndicatorsProcessing? = null
    private var spectrogramDataProcessing: SpectrogramDataProcessing? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val leqRecordsFlow = audioSource.audioSamples
        .flatMapConcat {
            val processedSamples = processRawSamples(it)
            flowOf(*processedSamples.toTypedArray())
        }
        .shareIn(scope = scope, started = SharingStarted.WhileSubscribed(), replay = 1)

    private val spectrogramDataFlow = audioSource.audioSamples
        .flatMapConcat {
            val processedSamples = processSpectrogramData(it)
            flowOf(*processedSamples.toTypedArray())
        }
        .shareIn(scope = scope, started = SharingStarted.WhileSubscribed(), replay = 1)


    // - LiveAudioService

    override val isRunning: StateFlow<Boolean> = audioSource.state
        .map { it == AudioSource.State.RUNNING }
        .stateIn(scope, initialValue = false, started = SharingStarted.Eagerly)

    override val audioSourceState: StateFlow<AudioSource.State> = audioSource.state


    override fun setupAudioSource() {
        // Setup audio source whenever microphone permission is granted.
        scope.launch {
            permissionService.getPermissionStateFlow(Permission.RECORD_AUDIO)
                .map { it == PermissionState.GRANTED }
                .collect { isPermissionGranted ->
                    if (isPermissionGranted) {
                        audioSource.setup()
                    }
                }
        }

        // Listen to audio source state to start it whenever it is ready
        scope.launch {
            audioSourceState.collect { state ->
                if (state == AudioSource.State.READY && startOnReady) {
                    audioSource.start()
                }
            }
        }
    }

    override fun releaseAudioSource() {
        scope.launch { audioSource.release() }
    }

    override fun startListening() {
        if (audioSourceState.value == AudioSource.State.UNINITIALIZED) {
            startOnReady = true
        } else {
            audioSource.start()
        }
    }

    override fun stopListening() {
        // Pause audio source, or cancel delayed start if needed
        audioSource.pause()
        startOnReady = false
    }

    override fun getLeqRecordsFlow(): Flow<LeqRecord> {
        return leqRecordsFlow
    }

    override fun getSpectrogramDataFlow(): Flow<SpectrogramData> {
        return spectrogramDataFlow
    }

    override fun getWeightedLeqFlow(
        splDecayRate: Double,
        windowTime: Duration,
    ): Flow<Double> {
        val levelDisplay = LevelDisplayWeightedDecay(splDecayRate, windowTime)

        return getLeqRecordsFlow().map {
            levelDisplay.getWeightedValue(it.laeq)
        }
    }

    override fun getWeightedLeqPerFrequencyBandFlow(
        splDecayRate: Double,
        windowTime: Duration,
    ): Flow<Map<Int, Double>> {
        var levelDisplayBands: Map<Int, LevelDisplayWeightedDecay>? = null

        return getLeqRecordsFlow().map { indicators ->
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


    // - Private functions

    private suspend fun processRawSamples(audioSamples: AudioSamples): List<LeqRecord> {
        if (indicatorsProcessing?.sampleRate != audioSamples.sampleRate) {
            logger.debug("Processing audio indicators with sample rate of ${audioSamples.sampleRate}")
            indicatorsProcessing = AcousticIndicatorsProcessing(audioSamples.sampleRate)
        }

        return indicatorsProcessing?.processSamples(audioSamples).orEmpty()
    }

    private fun processSpectrogramData(audioSamples: AudioSamples): List<SpectrogramData> {
        if (spectrogramDataProcessing?.sampleRate != audioSamples.sampleRate) {
            logger.debug("Processing spectrum data with sample rate of ${audioSamples.sampleRate}")
            val windowingMode = settingsService.get(SettingsKey.SettingWindowingMode)
            spectrogramDataProcessing = SpectrogramDataProcessing(
                sampleRate = audioSamples.sampleRate,
                windowSize = FFT_SIZE,
                windowHop = FFT_HOP,
                applyHannWindow = windowingMode == SpectrogramWindowingMode.HANN
            )
        }
        return spectrogramDataProcessing?.pushSamples(audioSamples.epoch, audioSamples.samples)
            .orEmpty().toList()
    }
}
