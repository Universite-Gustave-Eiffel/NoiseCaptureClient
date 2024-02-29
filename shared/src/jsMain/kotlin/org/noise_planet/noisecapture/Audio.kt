package org.noise_planet.noisecapture
import org.khronos.webgl.Float32Array
import web.media.streams.MediaStream

external class AudioContext {

    fun createMediaStreamSource(mediaStream: MediaStream): AudioNode
    fun createScriptProcessor(
        bufferSize: Int,
        numberOfInputChannels: Int,
        numberOfOutputChannels: Int
    ): ScriptProcessorNode
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
    val outputBuffer: AudioBuffer
}

external class ScriptProcessorNode : AudioNode {
    var onaudioprocess : (AudioProcessingEvent) -> Unit
    fun connect(node: AudioNode)
}

