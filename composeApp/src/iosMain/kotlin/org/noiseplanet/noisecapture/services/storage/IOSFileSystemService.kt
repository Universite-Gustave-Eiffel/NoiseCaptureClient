package org.noiseplanet.noisecapture.services.storage

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import org.noiseplanet.noisecapture.util.checkNoError
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask


@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IOSFileSystemService : FileSystemService {

    override fun getFileSize(fileUri: String): Long? {
        val filePath = NSURL.URLWithString(fileUri)?.path ?: return null

        return runCatching {
            memScoped {
                val error: ObjCObjectVar<NSError?> = alloc()
                val attributes = NSFileManager.defaultManager
                    .attributesOfItemAtPath(filePath, error.ptr)

                checkNoError(error.value) { "Could not get size of file at URL $filePath" }
                return attributes?.get(NSFileSize) as? Long
            }
        }.getOrNull()
    }

    override fun deleteFile(fileUri: String) {
        val absoluteUrl = getAbsolutePath(fileUri) ?: return
        val fileUrl = NSURL.URLWithString(absoluteUrl) ?: return

        memScoped {
            val error: ObjCObjectVar<NSError?> = alloc()
            NSFileManager.defaultManager.removeItemAtURL(fileUrl, error.ptr)

            checkNoError(error.value) { "Error while deleting file at URL $fileUrl" }
        }
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
        return url.absoluteString
    }
}
