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
        if (state == AudioSourceState.READY) {
            logger.debug("Audio source is already initialized, skipping setup.")
            return
        }
        
        // Create a recorder that will process raw incoming audio into audio samples
        // and broadcast it through the channel.
        audioRecorder = AudioRecorder(audioSamplesChannel, logger)
        state = AudioSourceState.READY
    }

    override fun start() {
        when (state) {
            AudioSourceState.UNINITIALIZED -> {
                logger.error("Audio source not initialized. Call setup() first.")
                return
            }

            AudioSourceState.RUNNING -> {
                logger.debug("Audio source already running.")
                return
            }

            AudioSourceState.READY -> {
                logger.debug("Audio source already paused.")
                // Start audio recording in a background thread and return the channel as a Flow
                audioThread = Thread(audioRecorder)
                audioThread?.start()
                state = AudioSourceState.RUNNING
            }
        }
    }

    override fun pause() {
        when (state) {
            AudioSourceState.UNINITIALIZED -> {
                logger.error("Audio source not initialized. Call setup() first.")
                return
            }

            AudioSourceState.RUNNING -> {
                audioRecorder?.stopRecording()
                state = AudioSourceState.READY
            }

            AudioSourceState.READY -> {
                logger.debug("Audio source already paused.")
                return
            }
        }
    }

    override fun release() {
        if (state == AudioSourceState.UNINITIALIZED) {
            logger.debug("Audio source already uninitialized, skipping cleanup.")
            return
        }

        pause()
        audioRecorder = null
        audioThread = null
        state = AudioSourceState.UNINITIALIZED
    }

    override fun getMicrophoneLocation(): MicrophoneLocation {
        return MicrophoneLocation.LOCATION_UNKNOWN
    }
}
