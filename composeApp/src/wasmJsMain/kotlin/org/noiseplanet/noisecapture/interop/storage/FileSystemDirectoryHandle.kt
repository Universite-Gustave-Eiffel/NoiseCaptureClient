@file:OptIn(ExperimentalWasmJsInterop::class)

package org.noiseplanet.noisecapture.interop.storage

import kotlin.js.Promise

/**
 * The FileSystemDirectoryHandle interface of the File System API provides a handle to a file system directory.
 *
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/FileSystemDirectoryHandle)
 */
external interface FileSystemDirectoryHandle : FileSystemHandle {

    /**
     * Returns a FileSystemDirectoryHandle for a subdirectory with the specified name
     * within the directory handle on which the method is called.
     *
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/FileSystemDirectoryHandle/getDirectoryHandle)
     *
     * @param name A string representing the FileSystemHandle.name of the subdirectory you wish to retrieve.
     * @param options An optional [FileSystemHandleOptions] containing options for the retrieved subdirectory.
     *
     * @return A [Promise] which resolves in a [FileSystemDirectoryHandle]
     */
    fun getDirectoryHandle(
        name: JsString,
        options: FileSystemHandleOptions = definedExternally,
    ): Promise<FileSystemDirectoryHandle>

    /**
     * Returns a FileSystemDirectoryHandle for a file with the specified name
     * within the directory handle on which the method is called.
     *
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/FileSystemDirectoryHandle/getFileHandle)
     *
     * @param name A string representing the FileSystemHandle.name of the file you wish to retrieve.
     * @param options An optional [FileSystemHandleOptions] containing options for the retrieved file.
     *
     * @return A [Promise] which resolves in a [FileSystemFileHandle]
     */
    fun getFileHandle(
        name: JsString,
        options: FileSystemHandleOptions = definedExternally,
    ): Promise<FileSystemFileHandle>

    /**
     * Attempts to remove an entry if the directory handle contains a file or directory called the name specified.
     *
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/FileSystemDirectoryHandle/removeEntry)
     *
     * @param name A string representing the FileSystemHandle.name of the entry you wish to remove.
     *
     * @return A [Promise] which resolves with undefined.
     */
    fun removeEntry(name: JsString): Promise<JsAny>
}
