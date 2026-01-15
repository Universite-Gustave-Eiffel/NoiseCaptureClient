package org.noiseplanet.noisecapture.services.storage

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.FilePickerEventBus
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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

    override suspend fun downloadFiles(fileUris: List<String>) {
        val cacheDir = context.cacheDir
        val zipFile = File(cacheDir, "archive.zip")

        // Create a zip file in the cache directory
        withContext(Dispatchers.IO) {
            ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
                fileUris.forEach { uri ->
                    val absolutePath = getAbsolutePath(uri) ?: return@forEach
                    val file = File(absolutePath)

                    if (file.exists()) {
                        FileInputStream(file).use { input ->
                            zipOut.putNextEntry(ZipEntry(uri))
                            input.copyTo(zipOut)
                            zipOut.closeEntry()
                        }
                    }
                }
            }
        }

        // Notify activity that a new file is ready to be downloaded through event bus
        filePickerEventBus.emitEvent(zipFile)

        // TODO: Figure out how to delete file only when it has been downloaded or dismissed
        // zipFile.delete()
    }

    override fun getRootDirectory(): String {
        return context.filesDir.absolutePath
    }
}
