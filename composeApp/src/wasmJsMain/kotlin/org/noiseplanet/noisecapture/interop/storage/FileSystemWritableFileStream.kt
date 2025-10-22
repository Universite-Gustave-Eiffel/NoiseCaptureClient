package org.noiseplanet.noisecapture.interop.storage

import kotlin.js.Promise

/**
 * A WritableStream object with additional convenience methods, which operates on a single file
 * on disk. The interface is accessed through the FileSystemFileHandle.createWritable() method.
 *
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/FileSystemWritableFileStream)
 */
@OptIn(ExperimentalWasmJsInterop::class)
external interface FileSystemWritableFileStream : JsAny {

    /**
     * Closes the stream.
     *
     * @return A [Promise] which fulfills with the undefined when all remaining chunks were
     *         successfully written before the close, or rejects with an error if a problem
     *         was encountered during the process.
     */
    fun close(): Promise<JsAny>

    /**
     * Writes content into the file the method is called on, at the current file cursor offset.
     *
     * @param data The file data to write, in the form of an ArrayBuffer, TypedArray, DataView,
     *             Blob, or string. Using an unsupported type might result in a crash.
     *
     * @return A [Promise] that returns undefined.
     */
    fun write(data: JsAny): Promise<JsAny>
}
