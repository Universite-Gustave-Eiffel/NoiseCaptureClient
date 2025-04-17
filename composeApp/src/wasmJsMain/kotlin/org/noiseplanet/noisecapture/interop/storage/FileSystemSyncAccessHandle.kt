package org.noiseplanet.noisecapture.interop.storage

import org.khronos.webgl.ArrayBuffer

/**
 * The FileSystemSyncAccessHandle interface of the File System API represents a synchronous handle
 * to a file system entry.
 *
 * This class is only accessible inside dedicated Web Workers (so that its methods do not block
 * execution on the main thread) for files within the origin private file system,
 * which is not visible to end-users.
 *
 * As a result, its methods are not subject to the same security checks as methods running on files
 * within the user-visible file system, and so are much more performant. This makes them suitable
 * for significant, large-scale file updates such as SQLite database modifications.
 *
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/FileSystemSyncAccessHandle)
 */
external interface FileSystemSyncAccessHandle : JsAny {

    /**
     * Closes an open synchronous file handle, disabling any further operations on it and releasing
     * the exclusive lock previously put on the file associated with the file handle.
     */
    fun close()

    /**
     * Persists any changes made to the file associated with the handle via the write()
     * method to disk.
     */
    fun flush()

    /**
     * Returns the size of the file associated with the handle in bytes.
     *
     * @return A number representing the size of the file in bytes.
     */
    fun getSize(): JsNumber

    /**
     * Resizes the file associated with the handle to a specified number of bytes.
     *
     * @param newSize The number of bytes to resize the file to.
     */
    fun truncate(newSize: JsNumber)

    /**
     * Writes the content of a specified buffer to the file associated with the handle,
     * optionally at a given offset.
     *
     * @param buffer An ArrayBuffer or ArrayBufferView (such as a DataView) representing the
     *               buffer to be written to the file.
     * @param options An options object containing the following properties:
     *                `at`:  A number representing the offset in bytes from the start of the file that
     *                the buffer should be written at.
     *
     * @return A number representing the number of bytes written to the file.
     */
    fun write(buffer: ArrayBuffer, options: FileSystemSyncAccessHandleWriteOptions): JsNumber
}

/**
 * Type utility for [FileSystemSyncAccessHandle.write] options.
 */
typealias FileSystemSyncAccessHandleWriteOptions = JsAny

/**
 * Builds a new [FileSystemSyncAccessHandleWriteOptions] JS object
 *
 * @param at A number representing the offset in bytes from the start of the file that the
 *           buffer should be written at. Defaults to zero.
 */
@Suppress("UnusedParameter")
fun fileSystemSyncAccessHandleWriteOptions(
    at: JsNumber = (0).toJsNumber(),
): FileSystemSyncAccessHandleWriteOptions = js("({ at: at })")
