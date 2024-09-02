package org.noiseplanet.noisecapture.storage

import android.content.Context
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.extensions.storeOf
import okio.Path.Companion.toPath
import org.noiseplanet.noisecapture.models.ApplicationData
import org.noiseplanet.noisecapture.models.STORAGE_VERSION
import java.io.File
import kotlin.io.path.readBytes

class AndroidStorageService(applicationContext: Context) : StorageService {
    private val appStorage: File = applicationContext.filesDir
    private val applicationDataPath = "$appStorage/appdata.json".toPath()

    override val store: KStore<ApplicationData>
        get() = storeOf(applicationDataPath, version = STORAGE_VERSION , default = ApplicationData())

    override suspend fun fetchDocumentRaw(documentId: String): ByteArray {
        return applicationDataPath.toNioPath().readBytes()
    }
}