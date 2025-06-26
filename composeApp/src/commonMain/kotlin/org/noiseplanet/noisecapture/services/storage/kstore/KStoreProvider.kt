package org.noiseplanet.noisecapture.services.storage.kstore

import io.github.xxfast.kstore.KStore
import kotlinx.serialization.Serializable

/**
 * Abstracts providing the store object itself to be adapted for each separate platform.
 *
 * We're using expect/actual pattern here instead of interface and dependency injection at
 * platform level because KStore relies on reified generics and this cannot be abstracted with
 * interfaces because of JVM limitations.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal expect class KStoreProvider() {

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
    inline fun <reified T : @Serializable Any> storeOf(
        key: String,
        enableCache: Boolean = true,
    ): KStore<T>
}
