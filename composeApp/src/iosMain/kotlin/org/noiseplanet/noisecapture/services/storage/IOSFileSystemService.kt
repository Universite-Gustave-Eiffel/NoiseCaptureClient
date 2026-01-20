package org.noiseplanet.noisecapture.services.storage

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ptr
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.util.injectLogger
import org.noiseplanet.noisecapture.util.runCatchingNSError
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSURL
import platform.Foundation.NSURLTypeIdentifierKey
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentInteractionController
import platform.UIKit.UIDocumentInteractionControllerDelegateProtocol
import platform.darwin.NSObject


@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
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
        val fileUrl = NSURL.fileURLWithPath(absoluteUrl)

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


private class UIDocumentInteractionControllerDelegate(
    private val onDismiss: () -> Unit,
) : NSObject(), UIDocumentInteractionControllerDelegateProtocol {

    override fun documentInteractionControllerDidDismissOptionsMenu(
        controller: UIDocumentInteractionController,
    ) {
        onDismiss()
    }
}
