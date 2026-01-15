package org.noiseplanet.noisecapture.services.storage

import kotlinx.browser.document
import kotlinx.coroutines.await
import org.noiseplanet.noisecapture.interop.storage.FileSystemWritableFileStream
import org.noiseplanet.noisecapture.util.OPFSHelper
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.File

/**
 * File system service using OPFS
 */
@OptIn(ExperimentalWasmJsInterop::class)
class OPFSFileSystemService : FileSystemService {

    // - Public functions

    override suspend fun getFileSize(fileUri: String): Long? {
        val (fileHandle, _) = OPFSHelper.getFileHandle(fileUri) ?: return null
        val file: File = fileHandle.getFile().await()

        return file.size.toInt().toLong()
    }

    override suspend fun deleteFile(fileUri: String) {
        val (fileHandle, directoryHandle) = OPFSHelper.getFileHandle(fileUri) ?: return
        directoryHandle.removeEntry(fileHandle.name).await<Unit>()
    }

    /**
     * Using OPFS, root storage point is just an empty URL.
     */
    override fun getRootDirectory(): String = ""

    override suspend fun downloadFile(fileUri: String) {
        // Get file blob
        val (fileHandle, _) = OPFSHelper.getFileHandle(fileUri) ?: return
        val file: File = fileHandle.getFile().await()

        // Create download URL
        val url = URL.createObjectURL(file)

        // Create anchor element and trigger the download
        val anchor = document.createElement("a") as HTMLAnchorElement
        anchor.href = url
        anchor.download = file.name
        document.body?.appendChild(anchor)
        anchor.click()

        // Clean up
        document.body?.removeChild(anchor)
        URL.revokeObjectURL(url)
    }

    /**
     * Store a Blob of audio in persistent storage.
     *
     * @param key Unique identifier of the audio file.
     * @param blob Audio data.
     */
    suspend fun store(key: String, blob: Blob) {
        // Get file handle and create a writable stream
        val (fileHandle, _) = OPFSHelper.getFileHandle(key, createIfNotFound = true) ?: return
        val stream: FileSystemWritableFileStream = fileHandle.createWritable().await()
        // Store raw data
        stream.write(blob).await<Unit>()
        // Close stream
        stream.close().await<Unit>()
    }
}
