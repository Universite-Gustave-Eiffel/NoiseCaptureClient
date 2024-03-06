package org.noise_planet.noisecapture

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Process
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import java.lang.Integer.max
import java.util.concurrent.atomic.AtomicBoolean
const val BUFFER_SIZE_TIME = 0.1
class AndroidAudioSource : AudioSource, Runnable {
    private lateinit var audioRecord: AudioRecord
    private var bufferSize = -1
    private var sampleRate = -1
    private val recording = AtomicBoolean(false)
    val audioSamplesChannel = Channel<AudioSamples>(onBufferOverflow = BufferOverflow.DROP_OLDEST)

    @SuppressLint("MissingPermission")
    override suspend fun setup(): Flow<AudioSamples> {
        if(this.bufferSize == -1) {
            val mSampleRates = intArrayOf(48000, 44100)
            val channel = AudioFormat.CHANNEL_IN_MONO
            val encoding = AudioFormat.ENCODING_PCM_FLOAT
            for (tryRate in mSampleRates) {
                this.sampleRate = tryRate
                val minimalBufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, encoding)
                if (minimalBufferSize == AudioRecord.ERROR_BAD_VALUE || minimalBufferSize == AudioRecord.ERROR) {
                    continue
                }
                this.bufferSize =
                    max(minimalBufferSize, (BUFFER_SIZE_TIME * sampleRate * 4).toInt())
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    sampleRate,
                    channel,
                    encoding,
                    bufferSize
                )
                Thread(this).start()
                break
            }
        }
        return audioSamplesChannel.consumeAsFlow()
    }

    override fun run() {
        recording.set(true)
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
        } catch (ex: IllegalArgumentException) {
            // Ignore
        } catch (ex: SecurityException) {
            // Ignore
        }
        audioRecord.startRecording()
        println("Capture microphone")
        var buffer = FloatArray(bufferSize / 4)
        while(recording.get()) {
            val read: Int = audioRecord.read(
                buffer, 0, buffer.size,
                AudioRecord.READ_BLOCKING
            )
            if (read < buffer.size) {
                buffer = buffer.copyOfRange(0, read)
                audioSamplesChannel.trySend(AudioSamples(System.currentTimeMillis(), buffer, AudioSamples.ErrorCode.OK, sampleRate))
            } else {
                audioSamplesChannel.trySend(AudioSamples(System.currentTimeMillis(), buffer.clone(), AudioSamples.ErrorCode.OK, sampleRate))
            }
        }
        bufferSize = -1
        audioSamplesChannel.trySend(AudioSamples(System.currentTimeMillis(), FloatArray(0),
            AudioSamples.ErrorCode.ABORTED, sampleRate))
        println("Release microphone")
    }

    override fun release() {
        if(audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord.stop()
        }
        recording.set(false)
    }

    override fun getMicrophoneLocation(): AudioSource.MicrophoneLocation {
        return AudioSource.MicrophoneLocation.LOCATION_UNKNOWN
    }
}
