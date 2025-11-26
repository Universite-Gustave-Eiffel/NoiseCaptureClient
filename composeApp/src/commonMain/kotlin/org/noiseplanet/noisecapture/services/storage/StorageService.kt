package org.noiseplanet.noisecapture.services.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement


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
     * Gets all ids of stored entities of this type.
     * Expected to be in chronological order from oldest to newest
     *
     * @return All ids of stored entities found in storage.
     */
    suspend fun getIndex(): List<String>

    /**
     * Gets a single entity from its unique identifier
     *
     * @param uuid Unique entity identifier
     *
     * @return Entity instance or null if not found in storage
     */
    suspend fun get(uuid: String): T?

    /**
     * Migrates the given entity from the given stored version to the current version. Triggered when
     * trying to deserialize a model unsuccessfully.
     *
     * By default, this function just removes the stored version from disk and returns null.
     * Override this method in model specific [StorageService] implementations to provide a proper
     * custom migration behaviour.
     *
     * @param uuid Unique entity identifier
     * @param currentVersion Version of the stored entity.
     * @param storedVersion Current entity version.
     * @param storedData Raw json data that couldn't be parsed automatically to the current entity version.
     */
    suspend fun migrate(
        uuid: String,
        currentVersion: Int,
        storedVersion: Int?,
        storedData: JsonElement?,
    ): T?

    /**
     * Gets the size of an entity in bytes from its unique identifier
     *
     * @param uuid Unique entity identifier
     *
     * @return Entity size in bytes or null if not found in storage
     */
    suspend fun getSize(uuid: String): Long?

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
     * Gest a [Flow] of entities ids that is updated every time a new entity is pushed or removed.
     * Expected to be in chronological order from oldest to newest.
     *
     * @return A [Flow] of lists of all entity ids.
     */
    fun subscribeIndex(): Flow<List<String>>

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
