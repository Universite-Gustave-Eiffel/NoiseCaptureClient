package org.noiseplanet.noisecapture.interop.storage

import org.w3c.files.File
import kotlin.js.Promise

/**
 * Represents a handle to a file system entry.
 *
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/FileSystemFileHandle)
 */
external interface FileSystemFileHandle : FileSystemHandle {

    /**
     * Returns a Promise which resolves to a File object representing the state on disk of the entry represented by the handle.
     *
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/FileSystemFileHandle/getFile)
     *
     * @return A Promise which resolves to a [File] object.
     */
    fun getFile(): Promise<File>

    /**
     * Returns a Promise which resolves to a FileSystemSyncAccessHandle object that can be used to
     * synchronously read from and write to a file. The synchronous nature of this method brings
     * performance advantages, but it is only usable inside dedicated Web Workers for files within
     * the origin private file system.
     *
     * Creating a FileSystemSyncAccessHandle takes an exclusive lock on the file associated with
     * the file handle. This prevents the creation of further FileSystemSyncAccessHandles or
     * FileSystemWritableFileStreams for the file until the existing access handle is closed.
     *
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/FileSystemFileHandle/createSyncAccessHandle)
     *
     * > NOTE: This can only run in a Web Worker. It is the most widely supported option but
     *         currently there is no other way to make use of Web Workers in Kotlin other than
     *         compiling a separate source set. It might be an option to add for later to
     *         support Safari.
     *
     * @param options Access options, see [FileSystemFileHandleOptions] for details
     *
     * @return A [Promise] which resolves to a [FileSystemSyncAccessHandle] object.
     */
    @Deprecated("")
    fun createSyncAccessHandle(
        options: FileSystemFileHandleOptions = definedExternally,
    ): Promise<FileSystemSyncAccessHandle>

    /**
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/FileSystemFileHandle/createSyncAccessHandle)
     *
     * @return A [Promise] which resolves to a [FileSystemWritableFileStream] object.
     */
    fun createWritable(): Promise<FileSystemWritableFileStream>
}

/**
 * Type utility for [FileSystemFileHandle] options.
 */
typealias FileSystemFileHandleOptions = JsAny

/**
 * Builds a new [FileSystemFileHandleOptions] JS object
 *
 * @param mode A string specifying the locking mode for the access handle. The default value is "readwrite".
 *             Possible values are:
 *
 * `"read-only"` Multiple FileSystemSyncAccessHandle objects can be opened simultaneously on a file
 *              (for example when using the same app in multiple tabs), provided they are all opened
 *              in "read-only" mode. Once opened, read-like methods can be called on the handles —
 *              read(), getSize(), and close().
 * `"readwrite"` Only one FileSystemSyncAccessHandle object can be opened on a file.
 *               Attempting to open subsequent handles before the first handle is closed results in
 *               a NoModificationAllowedError exception being thrown. Once opened, any available
 *               method can be called on the handle.
 * `"readwrite-unsafe"` Multiple FileSystemSyncAccessHandle objects can be opened simultaneously
 *                      on a file, provided they are all opened in "readwrite-unsafe" mode. Once
 *                      opened, any available method can be called on the handles.
 */
@Suppress("UnusedParameter")
fun fileSystemFileHandleOptions(
    mode: JsString = "read-only".toJsString(),
): FileSystemHandleOptions = js("({ create: create })")
