package org.noiseplanet.noisecapture.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.noiseplanet.noisecapture.util.getInputDevice
import kotlin.math.max
import kotlin.time.Clock

/**
 * Android audio source implementation
 */
@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
@SuppressLint("MissingPermission")
internal class AndroidAudioSource : AudioSource(), KoinComponent {

    // - Constants

    companion object {

        private const val BUFFER_SIZE_SECONDS = 0.125

        private const val CHANNEL = AudioFormat.CHANNEL_IN_MONO
        private const val ENCODING = AudioFormat.ENCODING_PCM_FLOAT

        private val SUITABLE_SAMPLE_RATES = listOf(48_000, 44_100)
    }


    // - Properties

    private val context: Context = get()
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioRecord: AudioRecord? = null


    // - Public functions

    override suspend fun setupInternal() {
        // Get suitable audio source configuration for the current device
        val (sampleRate, minBufferSize) = getSampleRateAndMinBufferSize() ?: run {
            logger.error("Could not get suitable sample rate (48.0 or 44.1 kHz)")
            return
        }
        val audioSourceConfig = getAudioSourceConfiguration()

        // Sample rate gives us the number of frames per second. Our refresh period in frames is
        // then equal to the buffer size in seconds times sample rate.
        val periodSizeInFrames = (sampleRate * BUFFER_SIZE_SECONDS).toInt()
        // Buffer size (in bytes) is equal to the number of frames * 4 (for ENCODING_PCM_FLOAT)
        val bufferSizeBytes = max(minBufferSize, periodSizeInFrames * 4)

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
    }

    override fun startInternal() {
        audioRecord?.startRecording()
    }

    override fun pauseInternal() {
        audioRecord?.stop()

    }

    override suspend fun releaseInternal() {
        pauseInternal()
        audioRecord = null
    }


    // - Private functions

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

            emitAudioSamples(
                AudioSamples(
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    samples = outputSamples,
                    sampleRate = it.sampleRate,
                )
            )
        }
    }
}
