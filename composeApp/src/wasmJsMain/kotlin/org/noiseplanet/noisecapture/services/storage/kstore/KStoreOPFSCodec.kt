package org.noiseplanet.noisecapture.services.storage.kstore

import io.github.xxfast.kstore.Codec
import io.github.xxfast.kstore.DefaultJson
import kotlinx.coroutines.await
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.noiseplanet.noisecapture.interop.navigator
import org.noiseplanet.noisecapture.interop.storage.FileSystemDirectoryHandle
import org.noiseplanet.noisecapture.interop.storage.FileSystemFileHandle
import org.noiseplanet.noisecapture.interop.storage.FileSystemWritableFileStream
import org.noiseplanet.noisecapture.interop.storage.fileSystemHandleOptions
import org.noiseplanet.noisecapture.log.Logger
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


/**
 * Custom KStore [Codec] implementation that serialises values to Json and stores
 * results in local storage using OPFS.
 *
 * @param filePath Path to the file that will be stored (e.g. "data/values.json").
 * @param json Json serialisation configuration.
 * @param serializer Serializer to use for values.
 * @param logger Logger for logging.
 */
class KStoreOPFSCodec<T : @Serializable Any>(
    private val filePath: String,
    private val json: Json,
    private val serializer: KSerializer<T>,
    private val logger: Logger,
) : Codec<T> {

    // - Codec

    override suspend fun decode(): T? {
        // Get file and directory handles
        val (fileHandle, _) = getFileHandle() ?: return null
        val file = fileHandle.getFile().await<File>()

        // Create file reader
        val reader = FileReader()
        // Read contents from file. Since FileReader relies on a callback to get the contents
        // after reading, we wrap this in a suspendCoroutine to synchronise the result
        val fileContents = suspendCoroutine { continuation ->
            reader.readAsText(file)
            reader.addEventListener("load") {
                // Continue execution when contents are available.
                continuation.resume(reader.result)
            }
        }
        // Decode JSON from string
        return json.decodeFromString(serializer, fileContents.toString())
    }

    override suspend fun encode(value: T?) {
        // Get file and directory handles, create them if not found
        val (fileHandle, directoryHandle) = getFileHandle(createIfNotFound = true) ?: return

        value?.let { unwrappedValue ->
            // Serialise data to JSON
            val data = json.encodeToString(serializer, unwrappedValue)
            // Get writer handle
            val stream: FileSystemWritableFileStream = fileHandle.createWritable().await()
            // Write data to file
            stream.write(data.toJsString()).await<Unit>()
            // Close writer handle
            stream.close().await<Unit>()
        } ?: run {
            // If value is none, remove file
            directoryHandle.removeEntry(fileHandle.name).await<Unit>()
        }
    }


    // - Private functions

    /**
     * Utility function to get an OPFS file handle from a file path  while creating
     * intermediate directories as needed.
     *
     * @param createIfNotFound If true, creates the file if not found. Else, return null if not found.
     *
     * @return Handles for the directory containing the file and the file itself.
     */
    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    private suspend fun getFileHandle(
        createIfNotFound: Boolean = false,
    ): Pair<FileSystemFileHandle, FileSystemDirectoryHandle>? {
        val storage = navigator?.storage ?: return null
        // Split file path in path components (dir names and file name)
        val pathComponents = filePath.split("/")
        val dirNames = pathComponents.dropLast(1)
        val fileName = pathComponents.last()

        return try {
            // Get OPFS root directory
            val opfsRoot: FileSystemDirectoryHandle = storage.getDirectory().await()

            // Set current directory to OPFS root
            var currentDirectory = opfsRoot

            // Create intermediary directories if they don't exist
            dirNames.forEach { dirName ->
                // Every time we create a new directory, update current directory handle
                currentDirectory = currentDirectory.getDirectoryHandle(
                    dirName.toJsString(),
                    options = fileSystemHandleOptions(create = createIfNotFound)
                ).catch {
                    throw FileNotFoundException()
                }.await()
            }
            // Get file handle, create it if necessary
            val fileHandle: FileSystemFileHandle = currentDirectory.getFileHandle(
                name = fileName.toJsString(),
                options = fileSystemHandleOptions(create = createIfNotFound)
            ).catch {
                throw FileNotFoundException()
            }.await()

            // Return directory and file handles
            Pair(fileHandle, currentDirectory)

        } catch (error: FileNotFoundException) {
            // If file doesn't exist, fail silently and return null
            null
        } catch (error: Exception) {
            logger.error(message = "An error occurred while access file storage", throwable = error)
            null
        }
    }
}

/**
 * Utility constructor for [KStoreOPFSCodec] using reified generic
 */
@Suppress("FunctionNaming")
inline fun <reified T : @Serializable Any> KStoreOPFSCodec(
    filePath: String,
    logger: Logger,
    json: Json = DefaultJson,
) = KStoreOPFSCodec<T>(
    filePath = filePath,
    json = json,
    serializer = json.serializersModule.serializer(),
    logger = logger,
)

private class FileNotFoundException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)
