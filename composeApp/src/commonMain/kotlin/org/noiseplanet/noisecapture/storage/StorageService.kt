package org.noiseplanet.noisecapture.storage

import io.github.xxfast.kstore.KStore
import kotlinx.serialization.Serializable


interface StorageService {
    /**
     * Retrieve parsed document with the specified document identifier
     */
    fun <T : @Serializable Any> fetchDocument(documentId: String) : KStore<T>

    /**
     * Retrieve document without parsing it. It is used when transferring measurements to server
     * not parsing the document reduce the local processing time
     */
    fun fetchDocumentRaw(documentId: String): ByteArray

}
