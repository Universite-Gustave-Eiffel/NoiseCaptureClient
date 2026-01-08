package org.noiseplanet.noisecapture.services.storage

import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class AndroidFileSystemService : FileSystemService, KoinComponent {

    // - Properties

    private val context: Context by inject()


    // - Public functions

    override fun getFileSize(fileUri: String): Long? {
        val file = File(fileUri)
        if (file.exists()) {
            return file.length()
        }
        return null
    }

    override fun deleteFile(fileUri: String) {
        val file = File(fileUri)
        if (file.exists()) {
            file.delete()
        }
    }

    override fun getAudioFilesDirectoryUri(): String? {
        return context.getExternalFilesDir(null)?.absolutePath
    }
}
