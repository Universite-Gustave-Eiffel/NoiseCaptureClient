package org.noiseplanet.noisecapture.services.storage

import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.FilePickerEventBus
import java.io.File

class AndroidFileSystemService : FileSystemService, KoinComponent {

    // - Properties

    private val context: Context by inject()
    private val filePickerEventBus: FilePickerEventBus by inject()


    // - Public functions

    override suspend fun getFileSize(fileUri: String): Long? {
        val absolutePath = getAbsolutePath(fileUri) ?: return null
        val file = File(absolutePath)
        if (file.exists()) {
            return file.length()
        }
        return null
    }

    override suspend fun deleteFile(fileUri: String) {
        val absolutePath = getAbsolutePath(fileUri) ?: return
        val file = File(absolutePath)
        if (file.exists()) {
            file.delete()
        }
    }

    override suspend fun downloadFile(fileUri: String) {
        // Read file contents
        val absolutePath = getAbsolutePath(fileUri) ?: return
        val file = File(absolutePath)

        // Notify activity that a new file is ready to be downloaded through event bus
        filePickerEventBus.emitEvent(file)
    }

    override fun getRootDirectory(): String {
        return context.filesDir.absolutePath
    }
}
