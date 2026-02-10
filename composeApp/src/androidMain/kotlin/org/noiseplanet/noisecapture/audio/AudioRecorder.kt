package org.noiseplanet.noisecapture.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioRecord.ERROR
import android.media.AudioRecord.ERROR_BAD_VALUE
import android.media.MediaRecorder
import android.os.Process
import kotlinx.coroutines.channels.Channel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.noiseplanet.noisecapture.log.Logger
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Processes audio coming through input microphone and broadcasts it through the given
 * [audioSamplesChannel]. Should be ran in a background thread.
 *
 * TODO: What happens if the user revokes permission while the app is in the background?
 */
@SuppressLint("MissingPermission")
class AudioRecorder(
    private val audioSamplesChannel: Channel<AudioSamples>,
    private val logger: Logger,
    private val targetAudioDeviceId: Int?,
) : Runnable, KoinComponent {

    // - Constants

    private companion object {

        private const val BUFFER_SIZE_TIME = 0.1
    }


    // - Properties

    private val context: Context = get()
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var audioRecord: AudioRecord
    private var bufferSize: Int
    private var sampleRate: Int
    private val isRecording = AtomicBoolean(false)


    // - Lifecycle

    init {
        val possibleSampleRates = intArrayOf(48_000, 44_100)
        val channel = AudioFormat.CHANNEL_IN_MONO
        val encoding = AudioFormat.ENCODING_PCM_FLOAT

        // Try to find a suitable sample rate for this device
        val (sampleRate, minBufferSize) = try {
            possibleSampleRates.map {
                Pair(it, AudioRecord.getMinBufferSize(it, channel, encoding))
            }.filterNot { (_, minBufferSize) ->
                minBufferSize == ERROR_BAD_VALUE || minBufferSize == ERROR
            }.first()
        } catch (err: NoSuchElementException) {
            val message = "Could not find a suitable sample rate"
            logger.error(message)
            logger.error(err.stackTraceToString())
            throw IllegalStateException(message, err)
        }
        bufferSize = Integer.max(
            minBufferSize,
            (BUFFER_SIZE_TIME * sampleRate * 4).toInt()
        )

        // If the device supports it, use UNPROCESSED audio source.
        // Otherwise, fallback to using VOICE_RECOGNITION (should not apply any noise cancelling
        // or automatic gain compensation).
        // https://developer.android.com/media/platform/mediarecorder#audiocapture
        val supportsUnprocessed = audioManager
            .getProperty(AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED)
            ?.toBoolean() == true
        val audioSource = if (supportsUnprocessed) {
            logger.debug("Using UNPROCESSED audio source")
            MediaRecorder.AudioSource.UNPROCESSED
        } else {
            logger.debug("Using VOICE_RECOGNITION audio source")
            MediaRecorder.AudioSource.VOICE_RECOGNITION
        }

        // Initialise AudioRecord with the target supported configuration
        audioRecord = AudioRecord(
            audioSource,
            sampleRate,
            channel,
            encoding,
            bufferSize
        )

        // If provided target device ID is found in available input devices,
        // use it as input microphone
        audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
            .firstOrNull { it.id == targetAudioDeviceId }
            .let { audioDeviceInfo ->
                audioRecord.setPreferredDevice(audioDeviceInfo)
            }

        this.sampleRate = sampleRate
    }


    // - Public functions

    override fun run() {
        isRecording.set(true)
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
        } catch (ignore: IllegalArgumentException) {
            // Ignore
        } catch (ignore: SecurityException) {
            // Ignore
        }
        try {
            audioRecord.startRecording()
            logger.debug("Capture microphone")
            while (isRecording.get()) {
                processBuffer()
            }
            broadcastAudioSamples(FloatArray(0), AudioSamples.ErrorCode.ABORTED)
            audioRecord.stop()
        } catch (e: IllegalStateException) {
            logger.error("${e.localizedMessage}\n${e.stackTraceToString()}")
        }
        isRecording.set(false)
        logger.debug("Release microphone")
    }

    /**
     * Stops the current audio recording
     */
    fun stopRecording() {
        isRecording.set(false)
    }


    // - Private functions

    /**
     * Processes the current audio data stored in the buffer
     */
    private fun processBuffer() {
        var buffer = FloatArray(bufferSize / 4)

        val read: Int = audioRecord.read(
            buffer,
            0,
            buffer.size,
            AudioRecord.READ_BLOCKING
        )

        if (read < buffer.size) {
            if (read > 0) {
                buffer = buffer.copyOfRange(0, read)
                broadcastAudioSamples(buffer)
            } else {
                broadcastAudioSamples(buffer.clone(), AudioSamples.ErrorCode.ABORTED)
                isRecording.set(false)
            }
        } else {
            broadcastAudioSamples(buffer.clone())
        }
    }

    /**
     * Broadcasts audio samples through the [audioSamplesChannel]
     */
    private fun broadcastAudioSamples(
        samples: FloatArray,
        errorCode: AudioSamples.ErrorCode? = null,
    ) {
        audioSamplesChannel.trySend(
            AudioSamples(
                System.currentTimeMillis(),
                samples,
                sampleRate,
                errorCode
            )
        )
    }
}
