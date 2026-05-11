package org.noiseplanet.noisecapture.services.storage

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ptr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.util.FilePickerEvent
import org.noiseplanet.noisecapture.util.IOSFilePickerEventBus
import org.noiseplanet.noisecapture.util.geo.FeatureCollection
import org.noiseplanet.noisecapture.util.geo.GeoJson
import org.noiseplanet.noisecapture.util.injectLogger
import org.noiseplanet.noisecapture.util.runCatchingNSError
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileCoordinator
import platform.Foundation.NSFileCoordinatorReadingForUploading
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.temporaryDirectory
import platform.Foundation.writeToURL
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class, ExperimentalTime::class)
class IOSFileSystemService : FileSystemService, KoinComponent {

    // - Properties

    private val logger: Logger by injectLogger()
    private val filePickerEventBus: IOSFilePickerEventBus by inject()

    private val fileManager: NSFileManager = NSFileManager.defaultManager


    // - FileSystemService

    override suspend fun getFileSize(fileUri: String): Long? {
        val filePath = getAbsolutePath(fileUri) ?: return null

        return runCatchingNSError { nsError ->
            fileManager.attributesOfItemAtPath(filePath, nsError.ptr)
        }.map {
            it?.get(NSFileSize) as? Long
        }.onFailure {
            logger.error("Could not get size of file at path $filePath")
        }.getOrNull()
    }

    override suspend fun deleteFile(fileUri: String) {
        val absolutePath = getAbsolutePath(fileUri) ?: return

        runCatchingNSError { nsError ->
            fileManager.removeItemAtPath(absolutePath, nsError.ptr)
        }.onFailure {
            logger.error("Error while deleting file at path $absolutePath")
        }
    }

    override suspend fun downloadFile(fileUri: String) {
        val absoluteUrl = getAbsolutePath(fileUri) ?: return
        val fileUrl = NSURL.fileURLWithPath(absoluteUrl)

        withContext(Dispatchers.Main) {
            downloadFileAtUrl(fileUrl)
        }
    }

    override suspend fun downloadFiles(fileUris: List<String>, archiveName: String) {
        val zipUrl = createZipInTmp(zipFileName = archiveName, filePathsToZip = fileUris) ?: return

        downloadFileAtUrl(zipUrl, deleteAfterUse = true)
    }

    override suspend fun downloadGeoJson(geoJson: FeatureCollection, fileName: String) {
        val contents = GeoJson.encodeToString(geoJson)
        val timestamp = Clock.System.now().toEpochMilliseconds().toString()
        val tempDirectory = fileManager.temporaryDirectory
            .URLByAppendingPathComponent(timestamp) // To avoid possible name clash, use unique timestamp
            ?: return
        val tempFile = tempDirectory.URLByAppendingPathComponent(fileName) ?: return

        // Create temporary directory
        runCatchingNSError { nsError ->
            fileManager.createDirectoryAtURL(
                tempDirectory, withIntermediateDirectories = true, null, nsError.ptr
            )
        }.onFailure {
            logger.error("Couldn't create temporary directory at path $tempDirectory", it)
            return
        }

        // Write contents to temporary file
        runCatchingNSError { nsError ->
            // Compiler warns about cast never succeeding but under the hood kotlin String maps to NSString
            @Suppress("CAST_NEVER_SUCCEEDS")
            (contents as? NSString)?.writeToURL(
                url = tempFile,
                atomically = true,
                encoding = NSUTF8StringEncoding,
                error = nsError.ptr
            )
        }.onFailure {
            logger.error("Couldn't write contents to file at path $tempFile", it)
            return
        }

        // Download created file, cleaning up after use
        withContext(Dispatchers.Main) {
            downloadFileAtUrl(tempFile, deleteAfterUse = true)
        }
    }

    /**
     * Get a URL to the ApplicationSupport directory, i.e. the app's internal storage directory.
     */
    override fun getRootDirectory(): String? {
        val urls = fileManager.URLsForDirectory(
            directory = NSApplicationSupportDirectory,
            inDomains = NSUserDomainMask
        )
        val url = urls.firstOrNull() as? NSURL? ?: return null
        return url.path
    }


    // - Private functions

    /**
     * Downloads the file at the given URL through [IOSFilePickerEventBus].
     *
     * @param fileUrl [NSURL] pointing to the file to download.
     * @param deleteAfterUse If true, delete the file once picker is dismissed
     */
    private suspend fun downloadFileAtUrl(fileUrl: NSURL, deleteAfterUse: Boolean = false) {
        filePickerEventBus.emitEvent(
            FilePickerEvent(
                fileUrl = fileUrl,
                onDismiss = {
                    // If needed, delete the file
                    if (deleteAfterUse) {
                        runCatchingNSError { nsError ->
                            fileManager.removeItemAtURL(fileUrl, nsError.ptr)
                        }.onFailure {
                            logger.error("Error while deleting file at url $fileUrl")
                        }
                    }
                }
            )
        )
    }

    /**
     * Creates a zip archive from the files at the given paths in temporary storage.
     *
     * @param zipFileName Archive file name.
     * @param zipExtension Archive extension, defaults to "zip".
     * @param filePathsToZip List of files to zip into archive.
     *
     * @return URL to the zip file in temporary directory or null if a failure occurs during compression.
     */
    @OptIn(ExperimentalTime::class, ExperimentalForeignApi::class)
    private fun createZipInTmp(
        zipFileName: String,
        zipExtension: String = "zip",
        filePathsToZip: List<String>,
    ): NSURL? {
        // Get the URL to the directory in temporary storage we will copy our files to.
        val timestamp = Clock.System.now().toEpochMilliseconds().toString()
        val directoryToZipUrl = fileManager.temporaryDirectory
            .URLByAppendingPathComponent(timestamp) // To avoid possible name clash, use unique timestamp
            ?.URLByAppendingPathComponent(zipFileName)
            ?: return null

        // Create temporary directory
        runCatchingNSError { nsError ->
            fileManager.createDirectoryAtURL(
                directoryToZipUrl, withIntermediateDirectories = true, null, nsError.ptr
            )
        }.onFailure {
            logger.error("Couldn't create temporary directory at path $directoryToZipUrl", it)
            return null
        }

        // Copy files to download in temporary directory
        filePathsToZip.forEach { filePath ->
            val absolutePath = getAbsolutePath(filePath) ?: return null
            val srcUrl = NSURL.fileURLWithPath(absolutePath)
            val toUrl = directoryToZipUrl.URLByAppendingPathComponent(filePath) ?: return null

            // Create intermediary directories if needed
            toUrl.URLByDeletingLastPathComponent?.let {
                runCatchingNSError { nsError ->
                    fileManager.createDirectoryAtURL(
                        url = it,
                        attributes = null,
                        withIntermediateDirectories = true,
                        error = nsError.ptr,
                    )
                }
            }

            runCatchingNSError { nsError ->
                fileManager.copyItemAtURL(srcURL = srcUrl, toURL = toUrl, error = nsError.ptr)
            }.onFailure {
                logger.error("Couldn't copy file to zip directory. src: $srcUrl, dest: $toUrl", it)
                return null
            }
        }

        val zipUrl = directoryToZipUrl.URLByAppendingPathExtension(zipExtension) ?: return null
        val coordinator = NSFileCoordinator()

        runCatchingNSError { nsError ->
            coordinator.coordinateReadingItemAtURL(
                url = directoryToZipUrl,
                options = NSFileCoordinatorReadingForUploading,
                error = nsError.ptr,
            ) { zipAccessUrl ->
                checkNotNull(zipAccessUrl) { "Could not create zip access URL" }
                fileManager.moveItemAtURL(srcURL = zipAccessUrl, toURL = zipUrl, nsError.ptr)
            }
        }.onFailure {
            logger.error("Error while creating zip file from $directoryToZipUrl to $zipUrl", it)
            return null
        }

        // Clear copied files from cache
        runCatchingNSError { nsError ->
            fileManager.removeItemAtURL(URL = directoryToZipUrl, error = nsError.ptr)
        }.onFailure {
            logger.warning("Error while cleaning up copied files at $directoryToZipUrl.", it)
        }

        return zipUrl
    }
}
