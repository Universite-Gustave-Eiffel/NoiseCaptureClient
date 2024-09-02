package org.noiseplanet.noisecapture.storage

import io.github.xxfast.kstore.KStore
import kotlinx.serialization.Serializable
import org.noiseplanet.noisecapture.models.ApplicationData
import org.noiseplanet.noisecapture.models.Record

/**
 * Default implementation of storage that does not persist data
 */
class DefaultStorageService : StorageService {
    private var applicationData = ApplicationData(mutableListOf(), mutableListOf())

    override val store: KStore<ApplicationData>
        get() = KStore(codec = DefaultStorageCodec(applicationData))


    override fun fetchDocumentRaw(documentId: String): ByteArray {
        TODO("Not yet implemented")
    }

}