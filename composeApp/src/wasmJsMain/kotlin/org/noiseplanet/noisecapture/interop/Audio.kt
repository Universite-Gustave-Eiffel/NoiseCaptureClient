package org.noiseplanet.noisecapture.interop

import org.khronos.webgl.Float32Array
import org.w3c.dom.mediacapture.MediaStream
import kotlin.js.Promise

/**
 * AudioContext Kotlin interop.
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/AudioContext)
 */
external class AudioContext {

    val destination: AudioDestinationNode

    fun close(): Promise<*>
    fun createMediaStreamSource(mediaStream: MediaStream): AudioNode
    fun createScriptProcessor(
        bufferSize: Int,
        numberOfInputChannels: Int,
        numberOfOutputChannels: Int,
    ): ScriptProcessorNode
}

/**
 * AudioDestinationNode Kotlin interop.
 * [MDN Reference](https://developer.mozilla.org/docs/Web/API/AudioDestinationNode)
 */
external class AudioDestinationNode : AudioNode {

    val maxChannelCount: Int
}

/**
 * AudioBuffer Kotlin interop.
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/AudioBuffer)
 */
external class AudioBuffer {

    val sampleRate: Float
    val length: Int
    val duration: Double
    val numberOfChannels: Int

    fun getChannelData(channel: Int): Float32Array
}

/**
 * AudioNode Kotlin interop.
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/AudioNode)
 */
open external class AudioNode {

    fun connect(
        destination: AudioNode,
        output: Int = definedExternally,
        input: Int = definedExternally,
    ): AudioNode

    fun disconnect()
}

/**
 * AudioProcessingEvent Kotlin interop.
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/AudioProcessingEvent)
 */
external class AudioProcessingEvent {

    val inputBuffer: AudioBuffer
}

/**
 * ScriptProcessorNode Kotlin interop.
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/ScriptProcessorNode)
 */
external class ScriptProcessorNode : AudioNode {

    var onaudioprocess: (AudioProcessingEvent) -> Unit
}
