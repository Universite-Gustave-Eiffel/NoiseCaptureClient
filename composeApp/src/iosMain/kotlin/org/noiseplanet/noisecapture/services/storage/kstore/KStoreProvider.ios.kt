package org.noiseplanet.noisecapture.services.storage.kstore

import io.github.xxfast.kstore.DefaultJson
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.extensions.VersionedCodec
import io.github.xxfast.kstore.storeOf
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.noiseplanet.noisecapture.util.NSFileManagerUtils
import org.noiseplanet.noisecapture.util.checkNoError
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSURL

/**
 * iOS KStoreProvider using local file storage and JSON encoding/decoding
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal actual class KStoreProvider {

    /**
     * Returns a [KStore] instance for the given file name.
     *
     * @param fileName Unique file name
     * @param enableCache If true, store value will be kept in memory until a new value is passed.
     *                    Note that this can have some memory impacts for large objects.
     * @param T Type of stored entity
     *
     * @return [KStore] object, created if necessary.
     */
    actual inline fun <reified T : @Serializable Any> storeOf(
        fileName: String,
        version: Int,
        noinline migration: Migration<T>,
        enableCache: Boolean,
    ): KStore<T> {
        val filePath = getFilePath(fileName)

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
        return storeOf(
            codec = VersionedCodec(
                file = filePath,
                version = version,
                migration = { version, data ->
                    runBlocking { migration(version, data) }
                },
                json = DefaultJson,
                serializer = DefaultJson.serializersModule.serializer(),
            ),
            enableCache = enableCache,
        )
    }

    /**
     * Gets the size of the given file.
     *
     * @param fileName Unique file name.
     *
     * @return File size in bytes, null if not found.
     */
    actual suspend fun sizeOf(fileName: String): Long? {
        // On iOS, audio URL is just the file name to avoid emulator sandboxing restrictions.
        val pathString = getFilePath(fileName).toString()
        val fileUrl = NSURL.URLWithString(pathString) ?: return null
        val filePath = fileUrl.path ?: return null

        memScoped {
            val error: ObjCObjectVar<NSError?> = alloc()
            val attributes =
                NSFileManager.defaultManager.attributesOfItemAtPath(filePath, error.ptr)
            checkNoError(error.value) { "Could not get size of file at URL $fileUrl" }

            return attributes?.get(NSFileSize) as? Long
        }
    }


    // - Private functions

    private fun getFilePath(fileName: String): Path {
        val documentsUrl = NSFileManagerUtils.getDocumentsDirectory()?.path
        checkNotNull(documentsUrl) { "Could not get documents directory URL" }

        // Get the path to the storage file for the given key
        return Path("$documentsUrl/$fileName")
    }
}
