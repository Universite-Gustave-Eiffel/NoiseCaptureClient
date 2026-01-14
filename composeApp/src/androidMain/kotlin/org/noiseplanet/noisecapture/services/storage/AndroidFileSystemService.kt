package org.noiseplanet.noisecapture.services.storage

import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class AndroidFileSystemService : FileSystemService, KoinComponent {

    // - Properties

    private val context: Context by inject()


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

    override fun getRootDirectory(): String {
        return context.filesDir.absolutePath
    }
}
