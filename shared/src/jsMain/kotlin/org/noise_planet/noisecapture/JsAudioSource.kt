package org.noise_planet.noisecapture

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.datetime.Clock
import org.khronos.webgl.get
import web.navigator.navigator

const val SAMPLES_BUFFER_SIZE = 512
const val AUDIO_CONSTRAINT = "{audio: {echoCancellation: false, autoGainControl: false, noiseSuppression: false}}"

class JsAudioSource : AudioSource {
    var micNode : AudioNode? = null
    //var dummyGainNode : GainNode? = null
    val audioSamplesChannel = Channel<AudioSamples>(onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override suspend fun setup(): Flow<AudioSamples> {
        println("Launch JSAudioSource")
        navigator.mediaDevices.getUserMedia(
            js(AUDIO_CONSTRAINT)
        ).then(onFulfilled = { mediaStream ->
            val audioContext = AudioContext()
            println("AudioContext ready $audioContext")
            micNode = audioContext.createMediaStreamSource(mediaStream)
            val scriptProcessorNode = audioContext.createScriptProcessor(SAMPLES_BUFFER_SIZE, 1, 1)
            scriptProcessorNode.onaudioprocess = { audioProcessingEvent ->
                val buffer = audioProcessingEvent.inputBuffer
                val jsBuffer = buffer.getChannelData(0)
                val samplesBuffer  = FloatArray(jsBuffer.length) { i -> jsBuffer[i] }
                audioSamplesChannel.trySend(AudioSamples(Clock.System.now().toEpochMilliseconds(), samplesBuffer,
                    AudioSamples.ErrorCode.OK, buffer.sampleRate.toInt()))
            }
            micNode!!.connect(scriptProcessorNode)
            //dummyGainNode = audioContext.createGain()
            //dummyGainNode!!.gain.value = 0F
            scriptProcessorNode.connect(audioContext.destination)
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
        audioSamplesChannel.close()
    }

    override fun getMicrophoneLocation(): AudioSource.MicrophoneLocation =
        AudioSource.MicrophoneLocation.LOCATION_UNKNOWN
}
