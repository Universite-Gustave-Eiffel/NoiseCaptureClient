package org.noiseplanet.noisecapture.services.storage

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.AndroidFilePickerEventBus
import org.noiseplanet.noisecapture.FilePickerEvent
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class AndroidFileSystemService : FileSystemService, KoinComponent {

    // - Properties

    override val dispatcher: CoroutineDispatcher
        get() = Dispatchers.IO

    private val context: Context by inject()
    private val filePickerEventBus: AndroidFilePickerEventBus by inject()


    // - Public functions

    override suspend fun download(fileUri: String) {
        // Read file contents
        val absolutePath = getAbsolutePath(fileUri) ?: return
        val file = File(absolutePath.toString())

        // Notify activity that a new file is ready to be downloaded through event bus
        filePickerEventBus.emitEvent(FilePickerEvent(file))
    }

    override suspend fun download(fileUris: List<String>, archiveName: String) {
        val cacheDir = context.cacheDir
        val zipFile = File(cacheDir, "$archiveName.zip")

        // Create a zip file in the cache directory
        withContext(Dispatchers.IO) {
            ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
                fileUris.forEach { uri ->
                    val absolutePath = getAbsolutePath(uri) ?: return@forEach
                    val file = File(absolutePath.toString())

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
        filePickerEventBus.emitEvent(FilePickerEvent(zipFile, deleteAfterUse = true))
    }

    override fun getRootDirectory(): String {
        return context.filesDir.absolutePath
    }
}
