package org.noiseplanet.noisecapture.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.services.audio.MicrophoneProviderService
import org.noiseplanet.noisecapture.util.injectLogger

/**
 * Common interface to access Audio samples from device microphone.
 * Each platform will supply its own implementation
 */
abstract class AudioSource : KoinComponent {

    // - Associated types

    /**
     * Describes the current state of an [AudioSource] instance
     */
    enum class State {

        /**
         * Audio source not initialized.
         * Must call [AudioSource.setup] before starting recording samples.
         */
        UNINITIALIZED,

        /**
         * Audio source initialized and ready to record.
         * Must call [AudioSource.start] to start recording samples.
         */
        READY,

        /**
         * Audio source currently recording incoming audio.
         */
        RUNNING,

        /**
         * Audio source was paused and can be started again.
         */
        PAUSED,
    }


    // - Properties

    protected val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    protected val microphoneProvider: MicrophoneProviderService = get()
    protected val logger: Logger by injectLogger()

    private val _audioSamples = MutableSharedFlow<AudioSamples>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        replay = 1,
    )
    private val _state = MutableStateFlow(State.UNINITIALIZED)

    /**
     * A flow of audio samples that will be emitted by the audio source whenever it
     * is currently recording.
     */
    val audioSamples: SharedFlow<AudioSamples> = _audioSamples

    /**
     * A flow of audio source states
     */
    val state: StateFlow<State> = _state


    // - Lifecycle

    init {
        // Subscribe to preferred input updates
        scope.launch {
            microphoneProvider.preferredInput.mapNotNull { it }
                .distinctUntilChanged { old, new ->
                    old.id == new.id
                }.collect {
                    onSelectedMicrophoneChange()
                }
        }
    }


    // - Public functions

    /**
     * Initializes the required components to start recording audio.
     * To start recording, call [AudioSource.start]
     */
    suspend fun setup() {
        if (state.value != State.UNINITIALIZED) {
            logger.debug("Audio source is already initialized, skipping setup.")
            return
        }
        runCatching { setupInternal() }
            .onSuccess { _state.tryEmit(State.READY) }
            .onFailure { logger.error("Error while setting up audio source", it) }
    }

    /**
     * Starts recording audio from an initialized audio source.
     * Should be called after [AudioSource.setup] or after [AudioSource.pause].
     */
    fun start() {
        when (state.value) {
            State.UNINITIALIZED -> {
                logger.warning("Audio source not initialized. Call setup() first.")
                return
            }

            State.RUNNING -> {
                logger.debug("Audio source already running.")
                return
            }

            State.READY, State.PAUSED -> {
                logger.debug("Starting audio source.")
                runCatching { startInternal() }
                    .onSuccess { _state.tryEmit(State.RUNNING) }
                    .onFailure { logger.error("Error while starting audio source", it) }
            }
        }
    }

    /**
     * Pauses recording, can be resumed afterwards by calling [AudioSource.start] again.
     * To stop recording and cleanup the underlying audio components, use [AudioSource.release].
     */
    fun pause() {
        when (state.value) {
            State.UNINITIALIZED -> {
                logger.warning("Audio source not initialized. Call setup() first.")
                return
            }

            State.RUNNING -> {
                logger.debug("Pausing audio source.")
                runCatching { pauseInternal() }
                    .onSuccess { _state.tryEmit(State.PAUSED) }
                    .onFailure { logger.error("Error while starting audio source", it) }
            }

            State.READY, State.PAUSED -> {
                logger.debug("Audio source already paused.")
                return
            }
        }
    }

    /**
     * Stops recording and cleans up underlying audio components.
     * To start recording again after releasing, one must call [AudioSource.setup] again before
     * [AudioSource.start]
     */
    suspend fun release() {
        if (state.value == State.UNINITIALIZED) {
            logger.debug("Audio source already uninitialized, skipping cleanup.")
            return
        }
        runCatching { releaseInternal() }
            .onSuccess { _state.tryEmit(State.UNINITIALIZED) }
            .onFailure { logger.error("Error while starting audio source", it) }
    }


    // - Protected functions

    /**
     * Platform specific implementation that sets up internal audio source.
     * If an error occurs during execution, should throw a detailed exception.
     */
    protected abstract suspend fun setupInternal()

    /**
     * Platform specific implementation that starts internal audio source.
     * If an error occurs during execution, should throw a detailed exception.
     */
    protected abstract fun startInternal()

    /**
     * Platform specific implementation that pauses internal audio source.
     * If an error occurs during execution, should throw a detailed exception.
     */
    protected abstract fun pauseInternal()

    /**
     * Platform specific implementation that releases internal audio source.
     * If an error occurs during execution, should throw a detailed exception.
     */
    protected abstract suspend fun releaseInternal()

    /**
     * Called when [MicrophoneProviderService]'s preferred input source is updated,
     * either due to manual user input or system notification.
     */
    protected fun onSelectedMicrophoneChange() {
        when (state.value) {
            // If audio source is setup but not running, setup again
            State.READY, State.PAUSED -> {
                scope.launch {
                    release()
                    setup()
                }
            }

            // If audio source is setup and running, setup again then start
            State.RUNNING -> {
                scope.launch {
                    release()
                    setup()
                    start()
                }
            }

            // Otherwise, do nothing
            State.UNINITIALIZED -> return
        }
    }

    /**
     * Broadcast new audio samples through internal shared flow.
     */
    protected fun emitAudioSamples(samples: AudioSamples) {
        _audioSamples.tryEmit(samples)
    }
}
