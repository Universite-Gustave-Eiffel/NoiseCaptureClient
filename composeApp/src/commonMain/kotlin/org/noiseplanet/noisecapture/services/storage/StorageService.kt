package org.noiseplanet.noisecapture.services.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable


/**
 * Wrapper for local storage access.
 * Provides platform specific implementations for reading and writing records to local storage.
 *
 * @param T Type of entity to be stored.
 */
interface StorageService<T : @Serializable Any> {

    companion object {

        /**
         * Gets the class name of a typed storage service, for dependency injection.
         *
         * @return Record type class name followed by "StorageService"
         */
        inline fun <reified T> className(): String {
            return "${T::class.simpleName!!}StorageService"
        }
    }

    /**
     * Gets all stored entities of this type
     *
     * @return All entities found in storage
     */
    suspend fun getAll(): List<T>

    /**
     * Gets a single entity from its unique identifier
     *
     * @param uuid Unique entity identifier
     *
     * @return Entity instance or null if not found in storage
     */
    suspend fun get(uuid: String): T?

    /**
     * Gets a [Flow] that is updated everytime a new entity is pushed or removed.
     *
     * Note: a new value won't be emitted if an existing entity is updated because that won't
     *       update the internal index.
     *
     * @return A [Flow] of lists of all entities.
     */
    fun subscribeAll(): Flow<List<T>>

    /**
     * Gets a [Flow] that is updated everytime the target entity is updated.
     * Emits null if the target entity is not found.
     *
     * @param uuid Unique entity identifier
     * @return A [Flow] of entity instances.
     */
    fun subscribeOne(uuid: String): Flow<T?>

    /**
     * Sets an entity's stored value.
     * If it doesn't exist yet, creates it.
     *
     * @param uuid Unique entity identifier
     * @param newValue New value to be stored
     */
    suspend fun set(uuid: String, newValue: T)

    /**
     * Deletes a stored entity.
     * If doesn't exist, does nothing.
     *
     * @param uuid Unique entity identifier
     */
    suspend fun delete(uuid: String)
}
