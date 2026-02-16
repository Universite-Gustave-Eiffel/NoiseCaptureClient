@file:OptIn(ExperimentalWasmJsInterop::class)

package org.noiseplanet.noisecapture.audio

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import org.khronos.webgl.get
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.interop.AudioContext
import org.noiseplanet.noisecapture.interop.AudioNode
import org.noiseplanet.noisecapture.interop.ScriptProcessorNode
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
internal class JsAudioSource : AudioSource(), KoinComponent {

    // - Constants

    companion object {

        const val SAMPLES_BUFFER_SIZE = 1024
    }


    // - Properties

    private var audioContext: AudioContext? = null
    private var micNode: AudioNode? = null
    private var scriptProcessorNode: ScriptProcessorNode? = null


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

    override suspend fun setupInternal() {
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
                scope.launch {
                    val timestamp = Clock.System.now().toEpochMilliseconds()

                    val buffer = audioProcessingEvent.inputBuffer
                    val jsBuffer = buffer.getChannelData(0)
                    val samplesBuffer = FloatArray(jsBuffer.length) { i -> jsBuffer[i] }

                    emitAudioSamples(
                        AudioSamples(
                            timestamp,
                            samplesBuffer,
                            buffer.sampleRate.toInt()
                        )
                    )
                }
            }
            mediaStream
        }, onRejected = { error ->
            throw IllegalStateException(error.toString())
        }).await<JsAny>()
    }

    override fun startInternal() {
        scriptProcessorNode?.let { scriptProcessorNode ->
            micNode?.connect(scriptProcessorNode)
            audioContext?.let { audioContext ->
                scriptProcessorNode.connect(audioContext.destination)
            }
        }
    }

    override fun pauseInternal() {
        micNode?.disconnect()
        scriptProcessorNode?.disconnect()
    }

    override suspend fun releaseInternal() {
        pauseInternal()
        audioContext?.close()
            ?.catch { error ->
                throw IllegalStateException(error.toString())
            }?.await<JsAny>()
    }
}
