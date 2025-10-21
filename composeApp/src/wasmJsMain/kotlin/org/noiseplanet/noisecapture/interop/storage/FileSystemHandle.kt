@file:OptIn(ExperimentalWasmJsInterop::class)

package org.noiseplanet.noisecapture.interop.storage

import kotlin.js.Promise

/**
 * The FileSystemHandle interface of the File System API is an object which represents a file or
 * directory entry. Multiple handles can represent the same entry. For the most part you do not
 * work with FileSystemHandle directly but rather its child interfaces
 * FileSystemFileHandle and FileSystemDirectoryHandle.
 *
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/FileSystemHandle)
 */
external interface FileSystemHandle : JsAny {

    /**
     * Returns the type of entry. This is 'file' if the associated entry is a file or 'directory'.
     *
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/FileSystemHandle/kind)
     */
    val kind: JsString

    /**
     * Returns the name of the associated entry.
     *
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/FileSystemHandle/name)
     */
    val name: JsString

    /**
     * Compares two handles to see if the associated entries (either a file or directory) match.
     *
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/FileSystemHandle/isSameEntry)
     */
    fun isSameEntry(other: FileSystemHandle): Promise<JsBoolean>
}

/**
 * Type utility for [FileSystemHandle] options.
 */
typealias FileSystemHandleOptions = JsAny

/**
 * Builds a new [FileSystemHandleOptions] JS object
 *
 * @param create When set to true if the directory or file is not found, one with the specified name
 *               will be created and returned.
 */
@Suppress("UnusedParameter")
fun fileSystemHandleOptions(
    create: Boolean = false,
): FileSystemHandleOptions = js("({ create: create })")
