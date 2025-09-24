package org.noiseplanet.noisecapture.services.storage.kstore

import io.github.xxfast.kstore.KStore
import kotlinx.coroutines.await
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.util.OPFSHelper
import org.noiseplanet.noisecapture.util.injectLogger
import org.w3c.files.File

/**
 * WasmJs KStore provider using key/value localstorage and JSON encoding/decoding
 */
@OptIn(ExperimentalWasmJsInterop::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal actual class KStoreProvider : KoinComponent {

    // - Properties

    private val logger: Logger by injectLogger()


    // - KStoreProvider

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
        enableCache: Boolean,
    ): KStore<T> {
        // For WasmJS, KStore doesn't support file storage out of the box so we use a custom codec
        // that will store the serialised values in JSON files using OPFS interop.
        return io.github.xxfast.kstore.storeOf(
            codec = KStoreOpfsCodec(
                filePath = fileName,
                logger = logger,
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
        val (fileHandle, _) = OPFSHelper.getFileHandle(fileName) ?: return null
        val file: File = fileHandle.getFile().await()

        return file.size.toInt().toLong()
    }
}
