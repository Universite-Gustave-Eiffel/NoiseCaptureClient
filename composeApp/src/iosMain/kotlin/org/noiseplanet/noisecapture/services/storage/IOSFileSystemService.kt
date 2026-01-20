package org.noiseplanet.noisecapture.services.storage

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ptr
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.util.injectLogger
import org.noiseplanet.noisecapture.util.runCatchingNSError
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileCoordinator
import platform.Foundation.NSFileCoordinatorReadingForUploading
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSURL
import platform.Foundation.NSURLTypeIdentifierKey
import platform.Foundation.NSUserDomainMask
import platform.Foundation.temporaryDirectory
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentInteractionController
import platform.UIKit.UIDocumentInteractionControllerDelegateProtocol
import platform.darwin.NSObject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class, ExperimentalTime::class)
class IOSFileSystemService : FileSystemService, KoinComponent {

    // - Properties

    private val logger: Logger by injectLogger()

    private var documentInteractionController: UIDocumentInteractionController? = null
    private val documentInteractionControllerDelegate = UIDocumentInteractionControllerDelegate(
        onDismiss = {
            // Drop reference to interaction controller
            documentInteractionController = null
        }
    )
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
            NSFileManager.defaultManager.removeItemAtPath(absolutePath, nsError.ptr)
        }.onFailure {
            logger.error("Error while deleting file at path $absolutePath")
        }
    }

    override suspend fun downloadFile(fileUri: String) {
        val absoluteUrl = getAbsolutePath(fileUri) ?: return
        val fileUrl = NSURL.fileURLWithPath(absoluteUrl)

        downloadFileAtUrl(fileUrl)
    }

    override suspend fun downloadFiles(fileUris: List<String>) {
        val zipUrl = createZipInTmp(zipFileName = "archive", filePathsToZip = fileUris) ?: return

        downloadFileAtUrl(zipUrl)
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
     * Downloads the file at the given URL through [UIDocumentInteractionController].
     *
     * @param fileUrl [NSURL] pointing to the file to download.
     */
    private fun downloadFileAtUrl(fileUrl: NSURL) {
        // Create and configure document interaction controller
        documentInteractionController = UIDocumentInteractionController
            .interactionControllerWithURL(fileUrl)

        // Get resource type identifier from URL, or default to "public.data"
        val uti = runCatchingNSError { nsError ->
            fileUrl.resourceValuesForKeys(listOf(NSURLTypeIdentifierKey), nsError.ptr)
                ?.get(NSURLTypeIdentifierKey) as? String?
                ?: "public.data"
        }.getOrNull()

        documentInteractionController?.UTI = uti
        documentInteractionController?.name = fileUrl.lastPathComponent
        documentInteractionController?.delegate = documentInteractionControllerDelegate

        UIApplication.sharedApplication.keyWindow?.rootViewController?.view?.let { view ->
            documentInteractionController?.presentOptionsMenuFromRect(
                rect = view.bounds,
                inView = view,
                animated = true,
            )
        }
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
                directoryToZipUrl,
                withIntermediateDirectories = true,
                attributes = null,
                error = nsError.ptr
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
                fileManager.moveItemAtURL(
                    srcURL = zipAccessUrl, toURL = zipUrl, error = nsError.ptr
                )
            }
        }.onFailure {
            logger.error("Error while creating zip file from $directoryToZipUrl to $zipUrl")
            return null
        }
        return zipUrl
    }
}


private class UIDocumentInteractionControllerDelegate(
    private val onDismiss: () -> Unit,
) : NSObject(), UIDocumentInteractionControllerDelegateProtocol {

    override fun documentInteractionControllerDidDismissOptionsMenu(
        controller: UIDocumentInteractionController,
    ) {
        onDismiss()
    }
}
