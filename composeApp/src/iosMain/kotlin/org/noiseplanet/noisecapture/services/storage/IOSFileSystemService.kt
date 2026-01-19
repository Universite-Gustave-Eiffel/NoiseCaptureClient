package org.noiseplanet.noisecapture.services.storage

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ptr
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.util.injectLogger
import org.noiseplanet.noisecapture.util.runCatchingNSError
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIDocumentInteractionController


@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IOSFileSystemService : FileSystemService, KoinComponent {

    // - Properties

    private val logger by injectLogger()


    // - FileSystemService

    override suspend fun getFileSize(fileUri: String): Long? {
        val filePath = getAbsolutePath(fileUri) ?: return null

        return runCatchingNSError { nsError ->
            NSFileManager.defaultManager.attributesOfItemAtPath(filePath, nsError.ptr)
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
        val fileUrl = NSURL.URLWithString(absoluteUrl) ?: return

        // Create and configure document interaction controller
        val viewController = UIDocumentInteractionController.interactionControllerWithURL(fileUrl)
        // TODO: Get file type identifier dynamically
        viewController.UTI = "public.data"
        viewController.name = fileUrl.lastPathComponent
        viewController.presentPreviewAnimated(animated = true)
    }

    override suspend fun downloadFiles(fileUris: List<String>) {
        TODO("Not yet implemented")
    }

    /**
     * Get a URL to the ApplicationSupport directory, i.e. the app's internal storage directory.
     */
    override fun getRootDirectory(): String? {
        val urls = NSFileManager.defaultManager.URLsForDirectory(
            directory = NSApplicationSupportDirectory,
            inDomains = NSUserDomainMask
        )
        val url = urls.firstOrNull() as? NSURL? ?: return null
        return url.path
    }
}
