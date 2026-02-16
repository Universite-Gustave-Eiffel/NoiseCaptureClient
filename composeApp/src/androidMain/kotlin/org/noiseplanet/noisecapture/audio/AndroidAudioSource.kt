package org.noiseplanet.noisecapture.audio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.audio.mic.MicrophoneProvider
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.util.getInputDevice
import org.noiseplanet.noisecapture.util.injectLogger
import kotlin.time.Clock

/**
 * Android audio source implementation
 */
@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
@SuppressLint("MissingPermission")
internal class AndroidAudioSource : AudioSource, KoinComponent {

    // - Constants

    companion object {

        private const val BUFFER_SIZE_SECONDS = 0.125

        private const val CHANNEL = AudioFormat.CHANNEL_IN_MONO
        private const val ENCODING = AudioFormat.ENCODING_PCM_FLOAT

        private val SUITABLE_SAMPLE_RATES = listOf(48_000, 44_100)
    }


    // - Properties

    private val audioSamplesChannel = Channel<AudioSamples>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val stateChannel = Channel<AudioSourceState>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val context: Context = get()
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioRecord: AudioRecord? = null

    private val microphoneProvider: MicrophoneProvider by inject()
    private val logger: Logger by injectLogger()

    override var state: AudioSourceState = AudioSourceState.UNINITIALIZED
        set(value) {
            field = value
            stateChannel.trySend(value)
        }

    override val audioSamples: Flow<AudioSamples> = audioSamplesChannel.receiveAsFlow()
    override val stateFlow: Flow<AudioSourceState> = stateChannel.receiveAsFlow()


    // - Lifecycle

    init {
        // Subscribe to preferred input updates
        scope.launch {
            microphoneProvider.preferredInput.mapNotNull { it }
                .distinctUntilChanged { old, new ->
                    old.id == new.id
                }.collect {
                    onSelectedMicrophoneChange()
                }
        }
    }


    // - Public functions

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override suspend fun setup() {
        if (state != AudioSourceState.UNINITIALIZED) {
            logger.debug("Audio source is already initialized, skipping setup.")
            return
        }

        // Get suitable audio source configuration for the current device
        val (sampleRate, _) = getSampleRateAndMinBufferSize() ?: run {
            logger.error("Could not get suitable sample rate (48.0 or 44.1 kHz)")
            return
        }
        val audioSourceConfig = getAudioSourceConfiguration()

        // Sample rate gives us the number of frames per second. Our refresh period in frames is
        // then equal to the buffer size in seconds times sample rate.
        val periodSizeInFrames = (sampleRate * BUFFER_SIZE_SECONDS).toInt()
        // Buffer size (in bytes) is equal to the number of frames * 4 (for ENCODING_PCM_FLOAT)
        val bufferSizeBytes = periodSizeInFrames * 4

        // Initialise AudioRecord with the target supported configuration
        audioRecord = AudioRecord(audioSourceConfig, sampleRate, CHANNEL, ENCODING, bufferSizeBytes)
            .apply {
                // Set an update listener that will fetch and process audio data every 125ms
                setRecordPositionUpdateListener(object : AudioRecord.OnRecordPositionUpdateListener {
                    override fun onMarkerReached(caller: AudioRecord?) {
                        // Do nothing
                    }

                    override fun onPeriodicNotification(caller: AudioRecord?) {
                        scope.launch {
                            // Process audio data in background thread
                            onAudioSamplesAvailable(bufferSizeBytes)
                        }
                    }
                })
                // Call our listener every n frames
                positionNotificationPeriod = periodSizeInFrames

                // If provided target device ID is found in available input devices,
                // use it as input microphone
                microphoneProvider.preferredInput.value?.id?.toIntOrNull()
                    ?.let { audioManager.getInputDevice(it) }
                    ?.let { preferredDevice = it }
            }

        state = AudioSourceState.READY
    }

    override fun start() {
        when (state) {
            AudioSourceState.UNINITIALIZED -> {
                logger.warning("Audio source not initialized. Call setup() first.")
                return
            }

            AudioSourceState.RUNNING -> {
                logger.debug("Audio source already running.")
                return
            }

            AudioSourceState.READY, AudioSourceState.PAUSED -> {
                logger.debug("Starting audio source.")
                audioRecord?.startRecording()
                state = AudioSourceState.RUNNING
            }
        }
    }

    override fun pause() {
        when (state) {
            AudioSourceState.UNINITIALIZED -> {
                logger.warning("Audio source not initialized. Call setup() first.")
                return
            }

            AudioSourceState.RUNNING -> {
                logger.debug("Pausing audio source.")
                // Stops recording and update state to notify UI
                audioRecord?.stop()
                state = AudioSourceState.PAUSED
            }

            AudioSourceState.READY, AudioSourceState.PAUSED -> {
                logger.debug("Audio source already paused.")
                return
            }
        }
    }

    override fun release() {
        if (state == AudioSourceState.UNINITIALIZED) {
            logger.debug("Audio source already uninitialized, skipping cleanup.")
            return
        }

        pause()
        state = AudioSourceState.UNINITIALIZED
    }


    // - Private functions

    /**
     * Called when [MicrophoneProvider]'s active device is updated, either due to manual user input
     * or system notification.
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun onSelectedMicrophoneChange() {
        when (state) {
            // If audio source is setup but not running, setup again
            AudioSourceState.READY, AudioSourceState.PAUSED -> {
                release()
                scope.launch { setup() }
            }

            // If audio source is setup and running, setup again then start
            AudioSourceState.RUNNING -> {
                release()
                scope.launch {
                    setup()
                    start()
                }
            }

            // Otherwise, do nothing
            AudioSourceState.UNINITIALIZED -> return
        }
    }

    /**
     * Tries to find a suitable sample rate for this device, as well as minimum required buffer size
     * for the current configuration.
     *
     * @return Sample rate, min buffer size. Null if no suitable sample rates were found.
     */
    private fun getSampleRateAndMinBufferSize(): Pair<Int, Int>? {
        return SUITABLE_SAMPLE_RATES
            .map { Pair(it, AudioRecord.getMinBufferSize(it, CHANNEL, ENCODING)) }
            .filterNot { (_, minBufferSize) ->
                minBufferSize == AudioRecord.ERROR_BAD_VALUE || minBufferSize == AudioRecord.ERROR
            }
            .firstOrNull()
    }

    /**
     * If the device supports it, use UNPROCESSED audio source.
     * Otherwise, fallback to using VOICE_RECOGNITION (should not apply any noise cancelling
     * or automatic gain compensation).
     *
     * https://developer.android.com/media/platform/mediarecorder#audiocapture
     */
    private fun getAudioSourceConfiguration(): Int {
        val supportsUnprocessed = audioManager
            .getProperty(AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED)
            ?.toBoolean() == true

        return if (supportsUnprocessed) {
            logger.debug("Using UNPROCESSED audio source")
            MediaRecorder.AudioSource.UNPROCESSED
        } else {
            logger.debug("Using VOICE_RECOGNITION audio source")
            MediaRecorder.AudioSource.VOICE_RECOGNITION
        }
    }

    /**
     * Called whenever audio samples are available for read
     */
    private fun onAudioSamplesAvailable(bufferSizeBytes: Int) {
        audioRecord?.let {
            // Initialize output buffer
            val buffer = FloatArray(bufferSizeBytes / 4)
            // Read incoming audio samples (use blocking read to get all samples at once)
            val read = it.read(buffer, 0, buffer.size, AudioRecord.READ_BLOCKING)

            val outputSamples = when {
                read <= 0 -> {
                    logger.error("Error while reading audio samples buffer: $read")
                    return
                }

                read < buffer.size -> {
                    // Could not read all audio samples, return a slice of the buffer corresponding
                    // to the samples that were actually read (should never happen in theory because
                    // we're using blocking read, but better safe than sorry)
                    buffer.sliceArray(0..<read)
                }

                // Read did complete as expected, return buffer as is
                else -> buffer
            }

            audioSamplesChannel.trySend(
                AudioSamples(
                    epoch = Clock.System.now().toEpochMilliseconds(),
                    samples = outputSamples,
                    sampleRate = it.sampleRate,
                )
            )
        }
    }
}
