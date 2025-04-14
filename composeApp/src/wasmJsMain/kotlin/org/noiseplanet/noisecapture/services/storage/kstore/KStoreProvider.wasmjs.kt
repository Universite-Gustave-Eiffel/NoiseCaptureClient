package org.noiseplanet.noisecapture.services.storage.kstore

import io.github.xxfast.kstore.KStore
import kotlinx.serialization.Serializable

/**
 * WasmJs KStore provider using key/value localstorage and JSON encoding/decoding
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal actual class KStoreProvider {

    /**
     * Returns a [KStore] instance for the given unique key.
     *
     * @param key Unique record key
     * @param T Type of stored entity
     *
     * @return [KStore] object, created if necessary.
     */
    actual inline fun <reified T : @Serializable Any> storeOf(key: String): KStore<T> {
        TODO("Not yet implemented")
    }
}
