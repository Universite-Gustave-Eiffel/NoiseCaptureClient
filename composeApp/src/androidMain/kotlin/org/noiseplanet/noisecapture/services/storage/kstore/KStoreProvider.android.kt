package org.noiseplanet.noisecapture.services.storage.kstore

import io.github.xxfast.kstore.DefaultJson
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.extensions.VersionedCodec
import io.github.xxfast.kstore.storeOf
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.services.storage.FileSystemService
import java.io.File


/**
 * Android KStoreProvider implementation using internal file storage and JSON for encoding/decoding
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal actual class KStoreProvider : KoinComponent {

    // - Properties

    private val fileSystemService: FileSystemService by inject()


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
        version: Int,
        noinline migration: Migration<T>,
        enableCache: Boolean,
    ): KStore<T> {
        val absolutePath = fileSystemService.getAbsolutePath(fileName)
        checkNotNull(absolutePath) { "Couldn't get absolute path for $fileName" }
        val file = File(absolutePath)
        // Create enclosing directory if it doesn't exist
        file.parentFile?.mkdirs()

        // Return KStore handle
        return storeOf(
            codec = VersionedCodec(
                file = Path(file.path),
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
        return fileSystemService.getFileSize(fileName)
    }
}
