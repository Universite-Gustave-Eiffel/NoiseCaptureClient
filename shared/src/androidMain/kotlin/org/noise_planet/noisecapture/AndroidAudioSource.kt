package org.noise_planet.noisecapture

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Process
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.lang.Integer.max
import java.util.concurrent.atomic.AtomicBoolean

const val SAMPLES_REPLAY = 10
const val SAMPLES_CACHE = 10
const val BUFFER_SIZE_TIME = 0.1
class AndroidAudioSource : AudioSource, Runnable {
    private lateinit var audioRecord: AudioRecord
    private var bufferSize = -1
    private var sampleRate = -1
    private val recording = AtomicBoolean(false)
    override val samples = MutableSharedFlow<AudioSamples>(replay = SAMPLES_REPLAY,
        extraBufferCapacity = SAMPLES_CACHE, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    @SuppressLint("MissingPermission")
    override fun setup(): AudioSource.InitializeErrorCode {
        if(this.bufferSize != -1) {
            return AudioSource.InitializeErrorCode.INITIALIZE_ALREADY_INITIALIZED
        }
        val mSampleRates = intArrayOf(48000, 44100)
        val channel = AudioFormat.CHANNEL_IN_MONO
        val encoding = AudioFormat.ENCODING_PCM_FLOAT
        for(tryRate in mSampleRates) {
            this.sampleRate = tryRate
            val minimalBufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, encoding)
            if(minimalBufferSize == AudioRecord.ERROR_BAD_VALUE || minimalBufferSize == AudioRecord.ERROR) {
                continue
            }
            this.bufferSize = max(minimalBufferSize,(BUFFER_SIZE_TIME * sampleRate * 4).toInt())
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate,
                channel,
                encoding,
                bufferSize
            )
            Thread(this).start()
            return AudioSource.InitializeErrorCode.INITIALIZE_OK
        }
        return AudioSource.InitializeErrorCode.INITIALIZE_SAMPLE_RATE_NOT_SUPPORTED
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
                samples.tryEmit(AudioSamples(System.currentTimeMillis(), buffer, AudioSamples.ErrorCode.OK))
            } else {
                samples.tryEmit(AudioSamples(System.currentTimeMillis(), buffer.clone(), AudioSamples.ErrorCode.OK))
            }
        }
        bufferSize = -1
        samples.tryEmit(AudioSamples(System.currentTimeMillis(), FloatArray(0),
            AudioSamples.ErrorCode.ABORTED))
        println("Release microphone")
    }

    override fun getSampleRate(): Int {
        return sampleRate
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
