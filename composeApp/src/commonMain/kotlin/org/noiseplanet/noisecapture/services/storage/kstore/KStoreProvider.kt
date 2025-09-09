package org.noiseplanet.noisecapture.services.storage.kstore

import io.github.xxfast.kstore.KStore
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Defines the signature of the migration method that will be called when unable to deserialize
 * an object due to version mismatch.
 *
 * @param storedVersion Stored entity version.
 * @param storedData Raw json data that couldn't be parsed automatically to the current entity version.
 */
internal typealias Migration<T> = suspend (
    storedVersion: Int?,
    storedData: JsonElement?,
) -> T?

/**
 * A default migration that does nothing and returns null.
 */
@Suppress("FunctionNaming", "FunctionName")
fun <T> DefaultMigration(): Migration<T> = { _, _ -> null }

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
     * Returns a [KStore] instance for the given file name.
     *
     * @param fileName Unique file name
     * @param enableCache If true, store value will be kept in memory until a new value is passed.
     *                    Note that this can have some memory impacts for large objects.
     * @param T Type of stored entity
     *
     * @return [KStore] object, created if necessary.
     */
    inline fun <reified T : @Serializable Any> storeOf(
        fileName: String,
        version: Int = 0,
        noinline migration: Migration<T> = DefaultMigration(),
        enableCache: Boolean = true,
    ): KStore<T>

    /**
     * Gets the size of the given file.
     *
     * @param fileName Unique file name.
     *
     * @return File size in bytes, null if not found.
     */
    suspend fun sizeOf(fileName: String): Long?
}
