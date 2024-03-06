package org.noise_planet.noisecapture

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.datetime.Clock
import org.khronos.webgl.get
import web.media.streams.MediaStream
import web.navigator.navigator

const val SAMPLES_BUFFER_SIZE = 1024
const val AUDIO_CONSTRAINT = "{audio: {echoCancellation: false, autoGainControl: false, noiseSuppression: false}}"

class JsAudioSource : AudioSource {
    var audioContext : AudioContext? = null
    var mediaStream : MediaStream? = null
    var micNode : AudioNode? = null
    var scriptProcessorNode : AudioNode? = null
    //var dummyGainNode : GainNode? = null
    val audioSamplesChannel = Channel<AudioSamples>(onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override suspend fun setup(): Flow<AudioSamples> {
            println("Launch JSAudioSource")
        navigator.mediaDevices.getUserMedia(
            js(AUDIO_CONSTRAINT)
        ).then(onFulfilled = { mediaStream ->
            this.mediaStream = mediaStream
            audioContext = AudioContext()
            println("AudioContext ready $audioContext.")
            micNode = audioContext!!.createMediaStreamSource(mediaStream)
            val scriptProcessorNode = audioContext!!.createScriptProcessor(SAMPLES_BUFFER_SIZE, 1, 1)
            scriptProcessorNode.onaudioprocess = { audioProcessingEvent ->
                val buffer = audioProcessingEvent.inputBuffer
                val jsBuffer = buffer.getChannelData(0)
                val samplesBuffer  = FloatArray(jsBuffer.length) { i -> jsBuffer[i] }
                audioSamplesChannel.trySend(AudioSamples(Clock.System.now().toEpochMilliseconds(), samplesBuffer,
                    AudioSamples.ErrorCode.OK, buffer.sampleRate.toInt()))
            }
            micNode!!.connect(scriptProcessorNode)
            scriptProcessorNode.connect(audioContext!!.destination)
            micNode!!.connect(scriptProcessorNode);
            AudioSource.InitializeErrorCode.INITIALIZE_OK
        }, onRejected = { jsError ->
            println("Error ${this::class} $jsError \n${jsError.stackTraceToString()}")
            AudioSource.InitializeErrorCode.INITIALIZE_NO_MICROPHONE
        }
        )
        return audioSamplesChannel.consumeAsFlow()
    }

    override fun release() {
        micNode?.disconnect()
        scriptProcessorNode?.disconnect()
        mediaStream?.getTracks()?.forEach { track -> track.stop() }
        try {
            audioContext?.close()!!.catch {
                // ignore
            }
        } catch (error : Exception) {
            // Ignore
            println(error.stackTraceToString())
        }
    }

    override fun getMicrophoneLocation(): AudioSource.MicrophoneLocation =
        AudioSource.MicrophoneLocation.LOCATION_UNKNOWN
}
