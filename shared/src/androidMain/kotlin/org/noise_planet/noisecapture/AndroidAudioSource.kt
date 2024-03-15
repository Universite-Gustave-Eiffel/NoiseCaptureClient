package org.noise_planet.noisecapture

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioRecord.ERROR
import android.media.AudioRecord.ERROR_BAD_VALUE
import android.media.AudioRecord.ERROR_INVALID_OPERATION
import android.media.MediaRecorder
import android.os.Binder
import android.os.IBinder
import android.os.Process
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import java.io.EOFException
import java.lang.IllegalStateException
import java.lang.Integer.max
import java.util.concurrent.atomic.AtomicBoolean
const val BUFFER_SIZE_TIME = 0.1
class AndroidAudioSource(private val context : Context) : AudioSource, Runnable, ServiceConnection {
    private lateinit var audioRecord: AudioRecord
    private var bufferSize = -1
    private var sampleRate = -1
    private val recording = AtomicBoolean(false)
    val audioSamplesChannel = Channel<AudioSamples>(onBufferOverflow = BufferOverflow.DROP_OLDEST)

    @SuppressLint("MissingPermission")
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
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
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        TODO("Not yet implemented")
    }

    override suspend fun setup(): Flow<AudioSamples> {
        val intent = Intent(context, MeasurementService::class.java)
        if(context.bindService(intent, this, Context.BIND_AUTO_CREATE)) {
            return audioSamplesChannel.consumeAsFlow()
        } else {
            throw IllegalStateException("Could not bind Android service AndroidAudioSource")
        }
    }

    fun readErrorCodeToString(errorCode : Int) : String {
        return when(errorCode) {
            ERROR_INVALID_OPERATION -> "ERROR_INVALID_OPERATION"
            ERROR_BAD_VALUE -> "ERROR_BAD_VALUE"
            ERROR -> "Other Error"
            else -> "Unknown error"
        }
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
                if(read > 0) {
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
                    audioSamplesChannel.close(EOFException("End of audio stream "+readErrorCodeToString(read)))
                    recording.set(false)
                    break
                }
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
