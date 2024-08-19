package org.noiseplanet.noisecapture.audio

import kotlinx.browser.window
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.Clock
import org.khronos.webgl.get
import org.noiseplanet.noisecapture.interop.AudioContext
import org.noiseplanet.noisecapture.interop.AudioNode
import org.noiseplanet.noisecapture.interop.ScriptProcessorNode
import org.noiseplanet.noisecapture.log.Logger
import org.w3c.dom.mediacapture.MediaStreamConstraints

/**
 * TODO: Document, cleanup, use platform logger instead of println, get rid of force unwraps (!!)
 */
internal class JsAudioSource(
    private val logger: Logger,
) : AudioSource {

    companion object {

        const val SAMPLES_BUFFER_SIZE = 1024
    }

    private var audioContext: AudioContext? = null
    private var micNode: AudioNode? = null
    private var scriptProcessorNode: ScriptProcessorNode? = null

    private val audioSamplesChannel = Channel<AudioSamples>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override suspend fun setup(): Flow<AudioSamples> {
        logger.debug("Launch JSAudioSource...")

        window.navigator.mediaDevices.getUserMedia(
            MediaStreamConstraints(
                // TODO: Not sure this has any effect...
                audio = object {
                    val echoCancellation = false
                    val autoGainControl = false
                    val noiseSuppression = false
                }.toJsReference()
            )
        ).then(onFulfilled = { mediaStream ->
            audioContext = AudioContext()
            logger.debug("AudioContext ready $audioContext.")

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
                logger.debug("New samples: ${audioProcessingEvent.inputBuffer}")

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
            scriptProcessorNode?.let { scriptProcessorNode ->
                micNode?.connect(scriptProcessorNode)
                audioContext?.let { audioContext ->
                    scriptProcessorNode.connect(audioContext.destination)
                }
            }
            mediaStream
        }, onRejected = { error ->
            logger.error("Error while setting up audio source: $error")
            error
        })
        return audioSamplesChannel.receiveAsFlow()
    }

    override fun release() {
        logger.debug("Releasing audio source")

        micNode?.disconnect()
        scriptProcessorNode?.disconnect()

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
    }

    override fun getMicrophoneLocation(): AudioSource.MicrophoneLocation =
        AudioSource.MicrophoneLocation.LOCATION_UNKNOWN
}
