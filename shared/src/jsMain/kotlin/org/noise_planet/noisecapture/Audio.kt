package org.noise_planet.noisecapture
import org.khronos.webgl.Float32Array
import web.media.streams.MediaStream

external class AudioContext {
    val destination: AudioDestinationNode

    fun createMediaStreamSource(mediaStream: MediaStream): AudioNode
    fun createScriptProcessor(
        bufferSize: Int,
        numberOfInputChannels: Int,
        numberOfOutputChannels: Int
    ): ScriptProcessorNode

    /**
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/BaseAudioContext/createGain)
     */
    fun createGain(): GainNode
}
external class AudioDestinationNode :
    AudioNode {
    /**
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/AudioDestinationNode/maxChannelCount)
     */
    val maxChannelCount: Int
}

external class AudioBuffer {
    val sampleRate: Float
    val length: Int
    val duration: Double
    val numberOfChannels: Int

    fun getChannelData(channel: Int): Float32Array
}

open external class AudioNode {
    fun connect(destination: AudioNode, output: Int = definedExternally,
                input: Int = definedExternally): AudioNode

    fun disconnect()

}

external class AudioProcessingEvent {
    val inputBuffer: AudioBuffer
}

external class AudioParam {

    /**
     * [MDN Reference](https://developer.mozilla.org/docs/Web/API/AudioParam/value)
     */
    var value: Float
}

external class GainNode : AudioNode {
    val gain: AudioParam
}

external class ScriptProcessorNode : AudioNode {
    var onaudioprocess : (AudioProcessingEvent) -> Unit
}

