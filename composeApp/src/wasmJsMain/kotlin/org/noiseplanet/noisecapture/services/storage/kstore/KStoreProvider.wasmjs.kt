package org.noiseplanet.noisecapture.services.storage.kstore

import io.github.xxfast.kstore.KStore
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.util.injectLogger

/**
 * WasmJs KStore provider using key/value localstorage and JSON encoding/decoding
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal actual class KStoreProvider : KoinComponent {

    // - Properties

    private val logger: Logger by injectLogger()


    // - KStoreProvider

    /**
     * Returns a [KStore] instance for the given unique key.
     *
     * @param key Unique record key
     * @param T Type of stored entity
     *
     * @return [KStore] object, created if necessary.
     */
    actual inline fun <reified T : @Serializable Any> storeOf(key: String): KStore<T> {
        // For WasmJS, KStore doesn't support file storage out of the box so we use a custom codec
        // that will store the serialised values in JSON files using OPFS interop.
        return io.github.xxfast.kstore.storeOf(
            codec = KStoreOPFSCodec(
                filePath = "$key.json",
                logger = logger,
            )
        )
    }
}
