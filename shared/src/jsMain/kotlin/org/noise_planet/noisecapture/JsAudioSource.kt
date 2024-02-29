package org.noise_planet.noisecapture

import js.promise.await
import kotlinx.datetime.Clock
import org.khronos.webgl.get
import web.navigator.navigator

const val SAMPLES_REPLAY = 10
const val SAMPLES_CACHE = 10
const val SAMPLES_BUFFER_SIZE = 512
const val BUFFER_SIZE_TIME = 0.1
const val AUDIO_CONSTRAINT = "{audio: {echoCancellation: false, optional: [ {autoGainControl: false}, {noiseSuppression: false} ]}}"

class JsAudioSource : AudioSource {
    var sampleRate = 0F
    var micNode : AudioNode? = null

    override suspend fun setup(callback : (audioSamples: AudioSamples)->Unit): AudioSource.InitializeErrorCode {
        println("Launch JSAudioSource")
        return navigator.mediaDevices.getUserMedia(
            js(AUDIO_CONSTRAINT)
        ).then(onFulfilled = { mediaStream ->
            val audioContext = AudioContext()
            println("AudioContext ready $audioContext")
            micNode = audioContext.createMediaStreamSource(mediaStream)
            val scriptProcessorNode = audioContext.createScriptProcessor(SAMPLES_BUFFER_SIZE, 1, 1)
            scriptProcessorNode.onaudioprocess = { audioProcessingEvent ->
                val buffer = audioProcessingEvent.outputBuffer
                sampleRate = buffer.sampleRate
                val jsBuffer = buffer.getChannelData(0)
                val samplesBuffer  = FloatArray(jsBuffer.length) { i -> jsBuffer[i] }
                callback(AudioSamples(Clock.System.now().toEpochMilliseconds(), samplesBuffer, AudioSamples.ErrorCode.OK))
            }
            micNode!!.connect(scriptProcessorNode)
            AudioSource.InitializeErrorCode.INITIALIZE_OK
        }, onRejected = { jsError ->
            println("Error ${this::class} $jsError \n${jsError.stackTraceToString()}")
            AudioSource.InitializeErrorCode.INITIALIZE_NO_MICROPHONE
        }
        ).await()
    }

    override fun getSampleRate(): Int = sampleRate.toInt()

    override fun release() {
        micNode?.disconnect()
    }

    override fun getMicrophoneLocation(): AudioSource.MicrophoneLocation =
        AudioSource.MicrophoneLocation.LOCATION_UNKNOWN
}
