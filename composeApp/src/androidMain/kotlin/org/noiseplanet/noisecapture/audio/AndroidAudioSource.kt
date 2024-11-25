package org.noiseplanet.noisecapture.audio

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.MicrophoneLocation

/**
 * Android audio source implementation
 *
 * @param logger Logger instance
 */
internal class AndroidAudioSource(
    private val logger: Logger,
) : AudioSource {

    private var audioThread: Thread? = null
    private var audioRecorder: AudioRecorder? = null

    private val audioSamplesChannel = Channel<AudioSamples>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val stateChannel = Channel<AudioSourceState>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override var state: AudioSourceState = AudioSourceState.UNINITIALIZED
        set(value) {
            field = value
            stateChannel.trySend(value)
        }

    override val audioSamples: Flow<AudioSamples> = audioSamplesChannel.receiveAsFlow()
    override val stateFlow: Flow<AudioSourceState> = stateChannel.receiveAsFlow()

    override fun setup() {
        // Create a recorder that will process raw incoming audio into audio samples
        // and broadcast it through the channel.
        audioRecorder = AudioRecorder(audioSamplesChannel, logger)
    }

    override fun release() {
        pause()
        audioRecorder = null
        audioThread = null
    }

    override fun start() {
        // Start audio recording in a background thread and return the channel as a Flow
        audioThread = Thread(audioRecorder)
        audioThread?.start()
    }

    override fun pause() {
        audioRecorder?.stopRecording()
    }

    override fun getMicrophoneLocation(): MicrophoneLocation {
        return MicrophoneLocation.LOCATION_UNKNOWN
    }
}
