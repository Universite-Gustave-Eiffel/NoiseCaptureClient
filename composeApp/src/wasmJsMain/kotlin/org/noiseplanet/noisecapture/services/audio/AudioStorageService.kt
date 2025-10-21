package org.noiseplanet.noisecapture.services.audio

import kotlinx.coroutines.await
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.interop.storage.FileSystemWritableFileStream
import org.noiseplanet.noisecapture.util.OPFSHelper
import org.w3c.files.Blob


/**
 * Store and retrieve audio files from storage.
 */
interface AudioStorageService {

    /**
     * Store a Blob of audio in persistent storage.
     *
     * @param key Unique identifier of the audio file.
     * @param blob Audio data.
     */
    suspend fun store(key: String, blob: Blob)

    /**
     * Retrieve a piece of audio from its unique identifier.
     *
     * @param key Unique identifier of the audio file.
     *
     * @return Audio data, if found.
     */
    suspend fun fetch(key: String): Blob?

    /**
     * Deletes the file with the given key if it exists
     *
     * @param key Unique identifier of the file to delete
     */
    suspend fun delete(key: String)
}


/**
 * [AudioStorageService] implementation using OPFS to store data persistently
 */
@OptIn(ExperimentalWasmJsInterop::class)
class OPFSAudioStorageService : AudioStorageService, KoinComponent {

    override suspend fun store(key: String, blob: Blob) {
        // Get file handle and create a writable stream
        val (fileHandle, _) = OPFSHelper.getFileHandle(key, createIfNotFound = true) ?: return
        val stream: FileSystemWritableFileStream = fileHandle.createWritable().await()
        // Store raw data
        stream.write(blob).await<Unit>()
        // Close stream
        stream.close().await<Unit>()
    }

    override suspend fun fetch(key: String): Blob? {
        // TODO: Not sure yet how this should be implemented since the media player bit is not
        //       there yet. Should it be a blob or a URL?
        return null
    }

    override suspend fun delete(key: String) {
        val (fileHandle, directoryHandle) = OPFSHelper.getFileHandle(key) ?: return
        directoryHandle.removeEntry(fileHandle.name).await<Unit>()
    }
}
