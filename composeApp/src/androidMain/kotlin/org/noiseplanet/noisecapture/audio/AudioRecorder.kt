package org.noiseplanet.noisecapture.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioRecord.ERROR
import android.media.AudioRecord.ERROR_BAD_VALUE
import android.media.MediaRecorder
import android.os.Process
import kotlinx.coroutines.channels.Channel
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
) : Runnable {

    private companion object {

        private const val BUFFER_SIZE_TIME = 0.1
    }

    private var audioRecord: AudioRecord
    private var bufferSize: Int
    private var sampleRate: Int
    private val isRecording = AtomicBoolean(false)

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
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channel,
            encoding,
            bufferSize
        )
        this.sampleRate = sampleRate
    }

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
