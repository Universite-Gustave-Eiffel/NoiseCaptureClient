package org.noiseplanet.noisecapture.services.storage.kstore

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.storeOf
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import org.noiseplanet.noisecapture.util.NSFileManagerUtils
import org.noiseplanet.noisecapture.util.checkNoError
import platform.Foundation.NSError
import platform.Foundation.NSFileManager

/**
 * iOS KStoreProvider using local file storage and JSON encoding/decoding
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal actual class KStoreProvider {

    /**
     * Returns a [KStore] instance for the given unique key.
     *
     * @param key Unique record key
     * @param enableCache If true, store value will be kept in memory until a new value is passed.
     *                    Note that this can have some memory impacts for large objects.
     * @param T Type of stored entity
     *
     * @return [KStore] object, created if necessary.
     */
    actual inline fun <reified T : @Serializable Any> storeOf(
        key: String,
        enableCache: Boolean,
    ): KStore<T> {
        val documentsUrl = NSFileManagerUtils.getDocumentsDirectory()?.path
        checkNotNull(documentsUrl) { "Could not get documents directory URL" }

        // Get the path to the storage file for the given key
        val filePath = Path("$documentsUrl/$key.json")

        // Create enclosing directories if they doesn't exist
        memScoped {
            val error: ObjCObjectVar<NSError?> = alloc()

            NSFileManager.defaultManager.createDirectoryAtPath(
                path = filePath.parent.toString(),
                attributes = null,
                withIntermediateDirectories = true,
                error = error.ptr
            )
            checkNoError(error.value) {
                "Error while creating intermediate directories at path ${filePath.parent}"
            }
        }

        // Return KStore handle
        return storeOf(file = filePath, enableCache = enableCache)
    }
}
