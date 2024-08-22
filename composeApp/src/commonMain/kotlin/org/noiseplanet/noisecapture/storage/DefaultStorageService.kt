package org.noiseplanet.noisecapture.storage

import io.github.xxfast.kstore.KStore
import kotlinx.serialization.Serializable

/**
 * Default implementation of storage do not persist data
 */
class DefaultStorageService : StorageService {
    val store: Map<String, @Serializable Any> = HashMap()

    override fun <T : Any> fetchDocument(documentId: String): KStore<T> {
        return KStore(codec = DefaultStorageCodec(store.get(documentId)))
    }

    override fun fetchDocumentRaw(documentId: String): ByteArray {
        TODO("Not yet implemented")
    }

}