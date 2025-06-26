package org.noiseplanet.noisecapture.services.storage.kstore

import android.content.Context
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.storeOf
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File


/**
 * Android KStoreProvider implementation using internal file storage and JSON for encoding/decoding
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal actual class KStoreProvider : KoinComponent {

    // - Properties

    val context: Context by inject()


    // - KStoreProvider

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
        // Build complete file path
        val filePath = Path("${context.filesDir}/$key.json")

        // Create enclosing directory if it doesn't exist
        File(filePath.parent.toString()).mkdirs()

        // Return KStore handle
        return storeOf(file = filePath, enableCache = enableCache)
    }
}
