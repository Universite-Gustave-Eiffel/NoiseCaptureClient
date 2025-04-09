package org.noiseplanet.noisecapture.services.storage.kstore

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.extensions.getOrEmpty
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.model.dao.LeqSequenceFragment
import org.noiseplanet.noisecapture.model.dao.LocationSequenceFragment
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.services.storage.StorageService
import kotlin.reflect.KClass


/**
 * Storage service implementation to work with KStore for handling multiplatform storage.
 *
 * Since all records are stored in a separate file (or local storage key) in KStore, we store a
 * separate index entry that will contain a list of all the records stored for this type.
 *
 * @param prefix will be added to all KStore stores paths for housekeeping purposes.
 *               (i.e. for measurements, all records will be stored under "{appDir}/measurements/{id}.json"
 * @param RecordType Type of entity to be stored
 */
class KStoreStorageService<RecordType : @Serializable Any>(
    private val prefix: String,
    private val type: KClass<RecordType>,
) : StorageService<RecordType>, KoinComponent {

    // - Properties

    private val storeProvider = KStoreProvider()


    // - StorageService

    override suspend fun getAll(): List<RecordType> {
        val indexStore: KStore<List<String>> = storeProvider.storeOf(key = "$prefix/index")
        val recordIds = indexStore.getOrEmpty()

        return recordIds.mapNotNull { item ->
            get(item)
        }
    }

    override suspend fun get(uuid: String): RecordType? {
        val store = getStoreForRecord(uuid)
        return store.get()
    }

    override suspend fun set(uuid: String, newValue: RecordType) {
        val store = getStoreForRecord(uuid)
        store.set(newValue)
    }

    override suspend fun delete(uuid: String) {
        val store = getStoreForRecord(uuid)
        store.delete()
    }


    // - Private functions

    /**
     * Gets the [KStore] handle for the record with the given UUID (creates it if needed).
     *
     * To get around the need of a reified type to create a [KStore] handle, we use a [KClass]
     * property that holds the type information of [RecordType], then using dynamic type checking
     * we can create the corresponding store.
     */
    @Suppress("UNCHECKED_CAST")
    private fun getStoreForRecord(uuid: String): KStore<RecordType> {
        val key = "$prefix/$uuid"

        // Check at runtime for record type
        return when (type) {
            Measurement::class -> storeProvider.storeOf<Measurement>(key)
            LeqSequenceFragment::class -> storeProvider.storeOf<LeqSequenceFragment>(key)
            LocationSequenceFragment::class -> storeProvider.storeOf<LocationSequenceFragment>(key)

            // Add other types that can be stored here.

            else -> throw UnsupportedOperationException("Trying to access unsupported storage type")
        } as KStore<RecordType>
    }
}
