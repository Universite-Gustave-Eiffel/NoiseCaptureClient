package org.noiseplanet.noisecapture.util

import kotlinx.coroutines.await
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.interop.navigator
import org.noiseplanet.noisecapture.interop.storage.FileSystemDirectoryHandle
import org.noiseplanet.noisecapture.interop.storage.FileSystemFileHandle
import org.noiseplanet.noisecapture.interop.storage.fileSystemHandleOptions
import org.noiseplanet.noisecapture.log.Logger


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
        return storage.getDirectory()
            .catch {
                throw OPFSUnavailableException("Cannot get root directory handle")
            }.await()
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

        return try {
            // Get OPFS root directory
            val opfsRoot = getOpfsRoot() ?: return null

            // Set current directory to OPFS root
            var currentDirectory = opfsRoot

            // Create intermediary directories if they don't exist
            dirNames.forEach { dirName ->
                // Every time we create a new directory, update current directory handle
                currentDirectory = currentDirectory.getDirectoryHandle(
                    dirName.toJsString(),
                    options = fileSystemHandleOptions(create = createIfNotFound)
                ).catch {
                    throw FileNotFoundException(dirName)
                }.await()
            }
            // Get file handle, create it if necessary
            val fileHandle: FileSystemFileHandle = currentDirectory.getFileHandle(
                name = fileName.toJsString(),
                options = fileSystemHandleOptions(create = createIfNotFound)
            ).catch {
                throw FileNotFoundException(filePath)
            }.await()

            // Return directory and file handles
            Pair(fileHandle, currentDirectory)

        } catch (error: FileNotFoundException) {
            logger.warning(message = "Could not access file or directory", throwable = error)
            null
        } catch (error: OPFSUnavailableException) {
            logger.error(message = "An error occurred while access file storage", throwable = error)
            null
        }
    }
}
