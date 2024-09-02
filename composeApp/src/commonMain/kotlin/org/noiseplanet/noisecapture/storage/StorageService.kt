package org.noiseplanet.noisecapture.storage

import io.github.xxfast.kstore.KStore
import kotlinx.serialization.Serializable
import org.noiseplanet.noisecapture.models.ApplicationData


interface StorageService {
    val store: KStore<ApplicationData>

    /**
     * Retrieve document without parsing it. It is used when transferring measurements to server
     * not parsing the document reduce the local processing time
     */
    suspend fun fetchDocumentRaw(documentId: String): ByteArray

}
