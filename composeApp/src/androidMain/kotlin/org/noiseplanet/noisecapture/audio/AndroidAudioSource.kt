package org.noiseplanet.noisecapture.audio

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.enums.MicrophoneLocation
import org.noiseplanet.noisecapture.util.injectLogger

/**
 * Android audio source implementation
 */
internal class AndroidAudioSource : AudioSource, KoinComponent {

    // - Properties

    private val audioSamplesChannel = Channel<AudioSamples>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val stateChannel = Channel<AudioSourceState>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private var audioRecorder: AudioRecorder? = null
    private var audioThread: Thread? = null

    private val logger: Logger by injectLogger()


    // - AudioSource

    override var state: AudioSourceState = AudioSourceState.UNINITIALIZED
        set(value) {
            field = value
            stateChannel.trySend(value)
        }

    override val audioSamples: Flow<AudioSamples> = audioSamplesChannel.receiveAsFlow()
    override val stateFlow: Flow<AudioSourceState> = stateChannel.receiveAsFlow()


    override fun setup() {
        if (state != AudioSourceState.UNINITIALIZED) {
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

            AudioSourceState.READY, AudioSourceState.PAUSED -> {
                logger.debug("Starting audio source.")
                // Start recording audio in a dedicated thread and update state to notify UI
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
                logger.debug("Pausing audio source.")
                // Stops recording and update state to notify UI
                audioRecorder?.stopRecording()
                audioThread?.join()
                state = AudioSourceState.PAUSED
            }

            AudioSourceState.READY, AudioSourceState.PAUSED -> {
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
        audioThread = null
        audioRecorder = null
        state = AudioSourceState.UNINITIALIZED
    }

    override fun getMicrophoneLocation(): MicrophoneLocation {
        return MicrophoneLocation.LOCATION_UNKNOWN
    }
}
