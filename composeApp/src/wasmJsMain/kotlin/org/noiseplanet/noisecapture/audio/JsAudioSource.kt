package org.noiseplanet.noisecapture.audio

import kotlinx.browser.window
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.khronos.webgl.get
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.interop.AudioContext
import org.noiseplanet.noisecapture.interop.AudioNode
import org.noiseplanet.noisecapture.interop.ScriptProcessorNode
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.enums.MicrophoneLocation
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

    private var audioContext: AudioContext? = null
    private var micNode: AudioNode? = null
    private var scriptProcessorNode: ScriptProcessorNode? = null

    private val audioSamplesChannel = Channel<AudioSamples>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val stateChannel = Channel<AudioSourceState>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )


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
        logger.debug("Setup JSAudioSource...")

        window.navigator.mediaDevices.getUserMedia(
            MediaStreamConstraints(
                audio = MediaTrackConstraints(
                    advanced = JsArray(),
                    autoGainControl = false.toJsBoolean(),
                    noiseSuppression = false.toJsBoolean(),
                    echoCancellation = false.toJsBoolean(),
                )
            )
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
        }
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

    override fun getMicrophoneLocation(): MicrophoneLocation =
        MicrophoneLocation.LOCATION_UNKNOWN
}
