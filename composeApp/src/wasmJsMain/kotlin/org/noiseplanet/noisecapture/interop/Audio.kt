@file:OptIn(ExperimentalWasmJsInterop::class)

package org.noiseplanet.noisecapture.interop

import org.khronos.webgl.Float32Array
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget
import org.w3c.dom.mediacapture.MediaStream
import org.w3c.files.Blob
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


/**
 * BlobEvent Kotlin interop.
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/BlobEvent)
 */
external class BlobEvent : Event {

    val data: Blob
}


/**
 * MediaRecorder Kotlin interop.
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/MediaRecorder)
 */
@Suppress("UnusedPrivateProperty")
external class MediaRecorder(
    mediaStream: MediaStream,
) : EventTarget {

    val state: JsString
    val stream: MediaStream

    val mimeType: JsString
    val audioBitsPerSecond: Long

    var onstop: (Event) -> Unit
    var ondataavailable: (BlobEvent) -> Unit

    fun pause()
    fun requestData()
    fun resume()
    fun start()
    fun stop()
}
