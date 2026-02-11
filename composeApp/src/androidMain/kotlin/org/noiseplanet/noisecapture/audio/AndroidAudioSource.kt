package org.noiseplanet.noisecapture.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.audio.mic.MicrophoneProvider
import org.noiseplanet.noisecapture.log.Logger
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

    private val scope = CoroutineScope(Dispatchers.IO)

    private val microphoneProvider: MicrophoneProvider by inject()
    private val logger: Logger by injectLogger()

    override var state: AudioSourceState = AudioSourceState.UNINITIALIZED
        set(value) {
            field = value
            stateChannel.trySend(value)
        }

    override val audioSamples: Flow<AudioSamples> = audioSamplesChannel.receiveAsFlow()
    override val stateFlow: Flow<AudioSourceState> = stateChannel.receiveAsFlow()


    // - Lifecycle

    init {
        // Subscribe to active microphone updates
        scope.launch {
            microphoneProvider.activeDevice.mapNotNull { it }
                .distinctUntilChanged { old, new ->
                    old.id == new.id
                }.collect {
                    onSelectedMicrophoneChange()
                }
        }
    }


    // - Public functions

    override fun setup() {
        if (state != AudioSourceState.UNINITIALIZED) {
            logger.debug("Audio source is already initialized, skipping setup.")
            return
        }
        state = AudioSourceState.READY
    }

    override fun start() {
        when (state) {
            AudioSourceState.UNINITIALIZED -> {
                logger.warning("Audio source not initialized. Call setup() first.")
                return
            }

            AudioSourceState.RUNNING -> {
                logger.debug("Audio source already running.")
                return
            }

            AudioSourceState.READY, AudioSourceState.PAUSED -> {
                logger.debug("Starting audio source.")
                // Create a recorder that will process raw incoming audio into audio samples
                // and broadcast it through the channel.
                audioRecorder = AudioRecorder(
                    audioSamplesChannel,
                    microphoneProvider.activeDevice.value?.id?.toIntOrNull()
                )
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
                logger.warning("Audio source not initialized. Call setup() first.")
                return
            }

            AudioSourceState.RUNNING -> {
                logger.debug("Pausing audio source.")
                // Stops recording and update state to notify UI
                audioRecorder?.stopRecording()
                audioThread?.join()
                audioThread = null
                audioRecorder = null
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
        state = AudioSourceState.UNINITIALIZED
    }


    // - Private functions

    /**
     * Called when [MicrophoneProvider]'s active device is updated, either due to manual user input
     * or system notification.
     */
    private fun onSelectedMicrophoneChange() {
        if (state == AudioSourceState.RUNNING) {
            // If audio source is already setup and running, pause and start it again so
            // it updates the active microphone
            pause()
            start()
        }
    }
}
