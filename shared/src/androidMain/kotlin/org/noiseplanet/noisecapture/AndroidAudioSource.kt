package org.noiseplanet.noisecapture

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioRecord.ERROR
import android.media.AudioRecord.ERROR_BAD_VALUE
import android.media.AudioRecord.ERROR_INVALID_OPERATION
import android.media.MediaRecorder
import android.os.Process
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import org.koin.core.logger.Logger
import java.util.concurrent.atomic.AtomicBoolean

const val BUFFER_SIZE_TIME = 0.1

class AndroidAudioSource(private val logger: Logger) : AudioSource {

    private val recording = AtomicBoolean(false)

    override suspend fun setup(): Flow<AudioSamples> {
        val audioSamplesChannel =
            Channel<AudioSamples>(onBufferOverflow = BufferOverflow.DROP_OLDEST)
        val audioThread = AudioThread(recording, audioSamplesChannel, logger)
        audioThread.startAudioRecord()
        return audioSamplesChannel.consumeAsFlow()
    }

    fun readErrorCodeToString(errorCode: Int): String {
        return when (errorCode) {
            ERROR_INVALID_OPERATION -> "ERROR_INVALID_OPERATION"
            ERROR_BAD_VALUE -> "ERROR_BAD_VALUE"
            ERROR -> "Other Error"
            else -> "Unknown error"
        }
    }

    override fun release() {
        recording.set(false)
    }

    override fun getMicrophoneLocation(): AudioSource.MicrophoneLocation {
        return AudioSource.MicrophoneLocation.LOCATION_UNKNOWN
    }
}

class AudioThread(
    private val recording: AtomicBoolean,
    private val audioSamplesChannel: Channel<AudioSamples>,
    private val logger: Logger,
) : Runnable {

    private lateinit var audioRecord: AudioRecord
    private var bufferSize = -1
    private var sampleRate = -1
    private var thread: Thread? = null

    @SuppressLint("MissingPermission")
    fun startAudioRecord() {
        if (this.bufferSize == -1) {
            val mSampleRates = intArrayOf(48000, 44100)
            val channel = AudioFormat.CHANNEL_IN_MONO
            val encoding = AudioFormat.ENCODING_PCM_FLOAT
            for (tryRate in mSampleRates) {
                this.sampleRate = tryRate
                val minimalBufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, encoding)
                if (minimalBufferSize == ERROR_BAD_VALUE || minimalBufferSize == ERROR) {
                    continue
                }
                this.bufferSize =
                    Integer.max(minimalBufferSize, (BUFFER_SIZE_TIME * sampleRate * 4).toInt())
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    sampleRate,
                    channel,
                    encoding,
                    bufferSize
                )
                thread = Thread(this)
                thread!!.start()
                return
            }
        }
    }

    override fun run() {
        recording.set(true)
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
            while (recording.get()) {
                if (!processRecord()) {
                    break
                }
            }
            bufferSize = -1
            audioSamplesChannel.trySend(
                AudioSamples(
                    System.currentTimeMillis(), FloatArray(0),
                    AudioSamples.ErrorCode.ABORTED, sampleRate
                )
            )
            audioRecord.stop()
        } catch (e: IllegalStateException) {
            logger.error("${e.localizedMessage}\n${e.stackTraceToString()}")
        }
        recording.set(false)
        logger.debug("Release microphone")
    }

    private fun processRecord(): Boolean {
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
                audioSamplesChannel.trySend(
                    AudioSamples(
                        System.currentTimeMillis(),
                        buffer,
                        AudioSamples.ErrorCode.OK,
                        sampleRate
                    )
                )
            } else {
                audioSamplesChannel.trySend(
                    AudioSamples(
                        System.currentTimeMillis(),
                        buffer.clone(),
                        AudioSamples.ErrorCode.ABORTED,
                        sampleRate
                    )
                )
                recording.set(false)
                return false
            }
        } else {
            audioSamplesChannel.trySend(
                AudioSamples(
                    System.currentTimeMillis(),
                    buffer.clone(),
                    AudioSamples.ErrorCode.OK,
                    sampleRate
                )
            )
        }
        return true
    }
}
