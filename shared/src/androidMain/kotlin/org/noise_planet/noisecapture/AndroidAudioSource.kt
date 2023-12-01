package org.noise_planet.noisecapture

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Process
import java.util.concurrent.atomic.AtomicBoolean

class AndroidAudioSource : AudioSource, Runnable {
    private lateinit var audioRecord: AudioRecord
    private lateinit var callback: AudioCallback
    private var bufferSize = -1
    private val recording = AtomicBoolean(false)
    @SuppressLint("MissingPermission")
    override fun setup(
        sampleRate: Int,
        bufferSize: Int,
        callback: AudioCallback
    ): AudioSource.InitializeErrorCode {
        if(this.bufferSize != -1) {
            return AudioSource.InitializeErrorCode.INITIALIZE_ALREADY_INITIALIZED
        }
        val channel = AudioFormat.CHANNEL_IN_MONO
        val encoding = AudioFormat.ENCODING_PCM_FLOAT
        val minimalBufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, encoding)
        when {
            minimalBufferSize == AudioRecord.ERROR_BAD_VALUE || minimalBufferSize == AudioRecord.ERROR  ->
                return AudioSource.InitializeErrorCode.INITIALIZE_SAMPLE_RATE_NOT_SUPPORTED
            minimalBufferSize > bufferSize ->
                return AudioSource.InitializeErrorCode.INITIALIZE_WRONG_BUFFER_SIZE
        }
        this.bufferSize = bufferSize
        audioRecord = AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, sampleRate, channel, encoding, bufferSize)
        this.callback = callback
        Thread(this).start()
        return AudioSource.InitializeErrorCode.INITIALIZE_OK
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
        var buffer = FloatArray(bufferSize / 4)
        while(recording.get()) {
            val read: Int = audioRecord.read(
                buffer, 0, buffer.size,
                AudioRecord.READ_BLOCKING
            )
            if (read < buffer.size) {
                buffer = buffer.copyOfRange(0, read)
            }
            callback(buffer)
        }
        bufferSize = -1
    }


    override fun release() {
        audioRecord.stop()
        recording.set(false)
    }

    override fun getMicrophoneLocation(): AudioSource.MicrophoneLocation {
        TODO("Not yet implemented")
    }
}
