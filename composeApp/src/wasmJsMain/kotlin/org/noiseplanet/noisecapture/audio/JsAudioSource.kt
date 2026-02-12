@file:OptIn(ExperimentalWasmJsInterop::class)

package org.noiseplanet.noisecapture.audio

import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.khronos.webgl.get
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.audio.mic.MicrophoneProvider
import org.noiseplanet.noisecapture.interop.AudioContext
import org.noiseplanet.noisecapture.interop.AudioNode
import org.noiseplanet.noisecapture.interop.ScriptProcessorNode
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.util.injectLogger
import org.w3c.dom.mediacapture.MediaStreamConstraints
import org.w3c.dom.mediacapture.MediaTrackConstraints
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


/**
 * WasmJS implementation of [AudioSource] interface
 *
 * For implementation details, see
 * [MDN web docs](https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API/Using_Web_Audio_API)
 */
@OptIn(ExperimentalTime::class)
internal class JsAudioSource : AudioSource, KoinComponent {

    // - Constants

    companion object {

        const val SAMPLES_BUFFER_SIZE = 1024
    }


    // - Properties

    private val logger: Logger by injectLogger()
    private val microphoneProvider: MicrophoneProvider by inject()

    private var audioContext: AudioContext? = null
    private var micNode: AudioNode? = null
    private var scriptProcessorNode: ScriptProcessorNode? = null

    private val audioSamplesChannel = Channel<AudioSamples>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val stateChannel = Channel<AudioSourceState>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val scope = CoroutineScope(Dispatchers.Default)

    override var state: AudioSourceState = AudioSourceState.UNINITIALIZED
        set(value) {
            field = value
            stateChannel.trySend(value)
        }

    override val audioSamples: Flow<AudioSamples> = audioSamplesChannel.receiveAsFlow()
    override val stateFlow: Flow<AudioSourceState> = stateChannel.receiveAsFlow()


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

    override suspend fun setup() {
        if (state != AudioSourceState.UNINITIALIZED) {
            logger.debug("Audio source is already initialized, skipping setup.")
            return
        }
        logger.debug("Setup JSAudioSource...")

        // Setup audio track constraints (asking for no AGC, noise suppression, etc)
        val audioConstraints = MediaTrackConstraints(
            advanced = JsArray(), // Useless but required otherwise the constraints object fails to parse
            autoGainControl = false.toJsBoolean(),
            noiseSuppression = false.toJsBoolean(),
            echoCancellation = false.toJsBoolean(),
        )

        // If a preferred input source is available, add it as an additional constraint
        // Note: Depending on the browser, it may trigger an additional microphone permission popup.
        microphoneProvider.preferredInput.value?.let {
            logger.debug("Selected device: ${it.label} (ID: ${it.id})")
            audioConstraints.deviceId = it.id.toJsString()
        }

        window.navigator.mediaDevices.getUserMedia(
            MediaStreamConstraints(audio = audioConstraints)
        ).then(onFulfilled = { mediaStream ->
            audioContext = AudioContext()

            micNode = audioContext?.createMediaStreamSource(mediaStream)
            checkNotNull(micNode) { "Failed initializing mic node" }

            scriptProcessorNode = audioContext?.createScriptProcessor(
                bufferSize = SAMPLES_BUFFER_SIZE,
                numberOfInputChannels = 1,
                numberOfOutputChannels = 1
            )
            checkNotNull(scriptProcessorNode) { "Failed initializing script processor node" }

            scriptProcessorNode?.onaudioprocess = { audioProcessingEvent ->
                val timestamp = Clock.System.now().toEpochMilliseconds()

                val buffer = audioProcessingEvent.inputBuffer
                val jsBuffer = buffer.getChannelData(0)
                val samplesBuffer = FloatArray(jsBuffer.length) { i -> jsBuffer[i] }

                audioSamplesChannel.trySend(
                    AudioSamples(
                        timestamp,
                        samplesBuffer,
                        buffer.sampleRate.toInt()
                    )
                )
            }
            state = AudioSourceState.READY
            mediaStream
        }, onRejected = { error ->
            logger.error("Error while setting up audio source: $error")
            error
        }).catch { error ->
            logger.error("Error while setting up audio source: $error")
            error
        }.await<JsAny>()
    }

    override fun start() {
        when (state) {
            AudioSourceState.UNINITIALIZED -> {
                logger.error("Audio source not initialized. Call setup() first.")
                return
            }

            AudioSourceState.RUNNING -> {
                logger.debug("Audio source already started.")
                return
            }

            AudioSourceState.READY, AudioSourceState.PAUSED -> {
                logger.debug("Starting audio recording")
                scriptProcessorNode?.let { scriptProcessorNode ->
                    micNode?.connect(scriptProcessorNode)
                    audioContext?.let { audioContext ->
                        scriptProcessorNode.connect(audioContext.destination)
                    }
                }
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
                micNode?.disconnect()
                scriptProcessorNode?.disconnect()
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

        logger.debug("Releasing audio source")
        pause()

        try {
            audioContext?.close()?.catch { error ->
                // ignore
                logger.error("Error while closing audio context: $error")
                error
            }
        } catch (ignore: Exception) {
            // Ignore
            logger.error("Uncaught exception:", ignore)
        }

        state = AudioSourceState.UNINITIALIZED
    }


    // - Private functions

    private fun onSelectedMicrophoneChange() {
        when (state) {
            // If audio source is setup but not running, setup again
            AudioSourceState.READY, AudioSourceState.PAUSED -> {
                release()
                scope.launch { setup() }
            }

            // If audio source is setup and running, setup again then start
            AudioSourceState.RUNNING -> {
                release()
                scope.launch {
                    setup()
                    start()
                }
            }

            // Otherwise, do nothing
            AudioSourceState.UNINITIALIZED -> return
        }
    }
}
