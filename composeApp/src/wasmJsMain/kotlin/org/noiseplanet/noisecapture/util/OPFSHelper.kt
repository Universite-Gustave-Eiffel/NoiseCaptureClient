package org.noiseplanet.noisecapture.util

import io.ktor.util.toJsArray
import kotlinx.coroutines.await
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.interop.navigator
import org.noiseplanet.noisecapture.interop.storage.FileSystemDirectoryHandle
import org.noiseplanet.noisecapture.interop.storage.FileSystemFileHandle
import org.noiseplanet.noisecapture.interop.storage.FileSystemWritableFileStream
import org.noiseplanet.noisecapture.interop.storage.fileSystemHandleOptions
import org.noiseplanet.noisecapture.log.Logger
import org.w3c.files.FileReader
import kotlin.coroutines.resume


/**
 * Utility object to access OPFS features
 */
@OptIn(ExperimentalWasmJsInterop::class)
object OPFSHelper : KoinComponent {

    // - Properties

    private val logger: Logger by injectLogger()


    // - Public functions

    /**
     * Gets the entry point of OPFS.
     *
     * @throws OPFSUnavailableException Thrown if an error occurs while trying to access OPFS.
     */
    suspend fun getOpfsRoot(): FileSystemDirectoryHandle? {
        val storage = navigator?.storage ?: return null

        // Specify that we want to store data persistently
        // For now there is no universal way of requiring persistent storage on all browsers,
        // so we just rely on best effort storage, meaning the browser might clear stored files
        // if storage space is running low.
        // storage.persist().catch {
        //     throw OPFSUnavailableException("Cannot access persistent storage")
        // }.await<JsBoolean>()

        // Get root directory handle
        return try {
            storage.getDirectory().await()
        } catch (error: JsException) {
            throw OPFSUnavailableException("Cannot get root directory handle", error)
        }
    }

    /**
     * Utility function to get an OPFS file handle from a file path  while creating
     * intermediate directories as needed.
     *
     * @param filePath File path, separated with slashes.
     * @param createIfNotFound If true, creates the file if not found. Else, return null if not found.
     *
     * @return Handles for the directory containing the file and the file itself.
     */
    @Suppress("TooGenericExceptionCaught")
    suspend fun getFileHandle(
        filePath: String,
        createIfNotFound: Boolean = false,
    ): Pair<FileSystemFileHandle, FileSystemDirectoryHandle>? {
        // Split file path in path components (dir names and file name)
        val pathComponents = filePath.split("/")
        val dirNames = pathComponents.dropLast(1)
        val fileName = pathComponents.last()

        // Get OPFS root directory
        val opfsRoot = getOpfsRoot() ?: return null

        // Set current directory to OPFS root
        var currentDirectory = opfsRoot

        // Create intermediary directories if they don't exist
        for (dirName in dirNames) {
            // Every time we create a new directory, update current directory handle
            currentDirectory = currentDirectory.getDirectoryHandle(
                dirName.toJsString(),
                options = fileSystemHandleOptions(create = true)
            ).catch { error ->
                logger.warning(
                    "Couldn't get directory handle for dir $dirName",
                    error.toThrowableOrNull()
                )
                null
            }.await() ?: return null
        }

        // Get file handle, create it if necessary
        val fileHandle: FileSystemFileHandle = currentDirectory.getFileHandle(
            name = fileName.toJsString(),
            options = fileSystemHandleOptions(create = createIfNotFound)
        ).catch { error ->
            logger.warning(
                "Couldn't get file handle for file $filePath",
                error.toThrowableOrNull()
            )
            null
        }.await() ?: return null

        // Return directory and file handles
        return Pair(fileHandle, currentDirectory)
    }

    /**
     * Reads contents of file at given path as [ByteArray].
     *
     * @param filePath
     * @return File contents or null if file not found.
     */
    suspend fun read(filePath: String): ByteArray? {
        // Get file and directory handles
        logger.debug("READ $filePath")
        val (fileHandle, _) = getFileHandle(filePath) ?: return null
        val file = fileHandle.getFile().await()

        // Create file reader
        val reader = FileReader()
        // Read contents from file. Since FileReader relies on a callback to get the contents
        // after reading, we wrap this in a suspendCoroutine to synchronise the result
        return suspendCancellableCoroutine { continuation ->
            reader.readAsArrayBuffer(file)
            reader.addEventListener("load") {
                // Continue execution when contents are available.
                (reader.result as? ArrayBuffer)?.let { arrayBuffer ->
                    val uint8Array = Uint8Array(arrayBuffer)
                    val byteArray = ByteArray(uint8Array.length) { index ->
                        uint8Array[index]
                    }
                    continuation.resume(byteArray)
                }
            }
        }
    }

    /**
     * Writes given data to file, creating it if it doesn't exist.
     *
     * @param filePath File path.
     * @param data Bytes to write.
     */
    suspend fun write(filePath: String, data: ByteArray) {
        // Get file handle, create it if not found
        val (fileHandle, _) = getFileHandle(filePath, createIfNotFound = true) ?: return
        // Get writer handle
        val stream: FileSystemWritableFileStream = fileHandle.createWritable().await()
        // Write data to file
        stream.write(data.toJsArray()).await()
        // Close writer handle
        stream.close().await()
    }
}
