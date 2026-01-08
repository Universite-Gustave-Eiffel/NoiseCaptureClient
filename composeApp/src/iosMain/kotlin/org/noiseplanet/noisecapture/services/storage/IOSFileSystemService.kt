package org.noiseplanet.noisecapture.services.storage

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import org.noiseplanet.noisecapture.util.NSFileManagerUtils
import org.noiseplanet.noisecapture.util.checkNoError
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSURL


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
        val fileUrl = NSURL.URLWithString(fileUri) ?: return

        memScoped {
            val error: ObjCObjectVar<NSError?> = alloc()
            NSFileManager.defaultManager.removeItemAtURL(fileUrl, error.ptr)

            checkNoError(error.value) { "Error while deleting file at URL $fileUrl" }
        }
    }

    override fun getAudioFilesDirectoryUri(): String? {
        return NSFileManagerUtils.getDocumentsDirectory()?.absoluteString
    }
}
