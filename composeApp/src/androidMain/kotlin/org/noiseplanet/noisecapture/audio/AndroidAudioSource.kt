package org.noiseplanet.noisecapture.audio

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import org.koin.core.logger.Logger

/**
 * Android audio source implementation
 *
 * @param logger Logger instance
 */
internal class AndroidAudioSource(private val logger: Logger) : AudioSource {

    private var audioThread: Thread? = null
    private var audioRecorder: AudioRecorder? = null

    override suspend fun setup(): Flow<AudioSamples> {
        val audioSamplesChannel = Channel<AudioSamples>(
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        // Create a recorder that will process raw incoming audio into audio samples
        // and broadcast it through the channel.
        audioRecorder = AudioRecorder(audioSamplesChannel, logger)
        // Start audio recording in a background thread and return the channel as a Flow
        audioThread = Thread(audioRecorder)
        audioThread?.start()
        return audioSamplesChannel.consumeAsFlow()
    }

    override fun release() {
        audioRecorder?.stopRecording()
    }

    override fun getMicrophoneLocation(): AudioSource.MicrophoneLocation {
        return AudioSource.MicrophoneLocation.LOCATION_UNKNOWN
    }
}
