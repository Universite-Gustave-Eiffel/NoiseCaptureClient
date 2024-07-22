package org.noiseplanet.noisecapture.audio

import kotlinx.browser.window
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.datetime.Clock
import org.khronos.webgl.get
import org.w3c.dom.mediacapture.MediaStream
import org.w3c.dom.mediacapture.MediaStreamConstraints

const val SAMPLES_BUFFER_SIZE = 1024
const val AUDIO_CONSTRAINT =
    "{audio: {echoCancellation: false, autoGainControl: false, noiseSuppression: false}}"

internal class JsAudioSource : AudioSource {

    private var audioContext: AudioContext? = null
    private var mediaStream: MediaStream? = null
    private var micNode: AudioNode? = null
    private var scriptProcessorNode: AudioNode? = null

    private val audioSamplesChannel = Channel<AudioSamples>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override suspend fun setup(): Flow<AudioSamples> {
        println("Launch JSAudioSource")
        window.navigator.mediaDevices.getUserMedia(
            MediaStreamConstraints(
                audio = object {
                    val audioCancellation = false
                }.toJsReference()
            )
        ).then(onFulfilled = { mediaStream ->
            println("Got it")

            this.mediaStream = mediaStream
            audioContext = AudioContext()
            println("AudioContext ready $audioContext.")
            micNode = audioContext!!.createMediaStreamSource(mediaStream)
            val scriptProcessorNode =
                audioContext!!.createScriptProcessor(SAMPLES_BUFFER_SIZE, 1, 1)
            scriptProcessorNode.onaudioprocess = { audioProcessingEvent ->
                val buffer = audioProcessingEvent.inputBuffer
                val jsBuffer = buffer.getChannelData(0)
                val samplesBuffer = FloatArray(jsBuffer.length) { i -> jsBuffer[i] }
                audioSamplesChannel.trySend(
                    AudioSamples(
                        Clock.System.now().toEpochMilliseconds(), samplesBuffer,
                        AudioSamples.ErrorCode.OK, buffer.sampleRate.toInt()
                    )
                )
            }
            micNode!!.connect(scriptProcessorNode)
            scriptProcessorNode.connect(audioContext!!.destination)
            micNode!!.connect(scriptProcessorNode)

            AudioSource.InitializeErrorCode.INITIALIZE_OK as JsAny
        }, onRejected = { error ->
            println("Error! $error")

            AudioSource.InitializeErrorCode.INITIALIZE_NO_MICROPHONE as JsAny
        })
        return audioSamplesChannel.consumeAsFlow()
    }

    override fun release() {
        micNode?.disconnect()
        scriptProcessorNode?.disconnect()

        try {
            audioContext?.close()?.catch { error ->
                // ignore
                println(error)
                error
            }
        } catch (ignore: Exception) {
            // Ignore
            println(ignore.stackTraceToString())
        }
    }

    override fun getMicrophoneLocation(): AudioSource.MicrophoneLocation =
        AudioSource.MicrophoneLocation.LOCATION_UNKNOWN
}
