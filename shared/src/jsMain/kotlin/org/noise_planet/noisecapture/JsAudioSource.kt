package org.noise_planet.noisecapture

import js.array.ReadonlyArray
import js.objects.Record
import js.promise.await
import js.typedarrays.Float32Array
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import web.audio.AudioContext
import web.audio.AudioWorkletGlobalScope
import web.audio.AudioWorkletNode
import web.audio.AudioWorkletProcessor
import web.audio.AudioWorkletProcessorImpl
import web.media.streams.MediaStreamTrack
import web.messaging.MessageEvent
import web.navigator.navigator

const val SAMPLES_REPLAY = 10
const val SAMPLES_CACHE = 10
const val BUFFER_SIZE_TIME = 0.1
const val AUDIO_CONSTRAINT = "{audio: {echoCancellation: false, optional: [ {autoGainControl: false}, {noiseSuppression: false} ]}}"

class JsAudioSource : AudioSource {

    override val samples = MutableSharedFlow<AudioSamples>(replay = SAMPLES_REPLAY,
        extraBufferCapacity = SAMPLES_CACHE, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override suspend fun setup(): AudioSource.InitializeErrorCode {
        println("Launch JSAudioSource")
        return navigator.mediaDevices.getUserMedia(
            js(AUDIO_CONSTRAINT)
        ).then(onFulfilled = { mediaStream ->
            val audioContext = AudioContext()
            println("AudioContext ready $audioContext")
            audioContext.audioWorklet.addModule("raw_audio_processor.js")
            val micNode = audioContext.createMediaStreamSource(mediaStream)
            val audioCaptureNode = AudioWorkletNode(audioContext, "raw_audio_processor")
            audioCaptureNode.port.onmessage = {
                event:MessageEvent<*> -> println("$event")
            }
            micNode.connect(audioCaptureNode)
            AudioSource.InitializeErrorCode.INITIALIZE_OK
        }, onRejected = { jsError ->
            println("Error ${this::class} $jsError \n${jsError.stackTraceToString()}")
            AudioSource.InitializeErrorCode.INITIALIZE_NO_MICROPHONE
        }
        ).await()
    }

    override fun getSampleRate(): Int = 48000

    override fun release() {

    }

    override fun getMicrophoneLocation(): AudioSource.MicrophoneLocation =
        AudioSource.MicrophoneLocation.LOCATION_UNKNOWN
}

class RawAudioProcessor : AudioWorkletProcessorImpl {
    override fun process(
        inputs: ReadonlyArray<ReadonlyArray<Float32Array>>,
        outputs: ReadonlyArray<ReadonlyArray<Float32Array>>,
        parameters: Record<String, Float32Array>
    ): Boolean {
        port.postMessage(inputs[0][0])
        return true
    }
}