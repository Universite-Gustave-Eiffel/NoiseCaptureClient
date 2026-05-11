package org.noiseplanet.noisecapture.services.storage

import kotlinx.browser.document
import kotlinx.coroutines.await
import org.noiseplanet.noisecapture.interop.ZipJs
import org.noiseplanet.noisecapture.interop.storage.FileSystemWritableFileStream
import org.noiseplanet.noisecapture.util.OPFSHelper
import org.noiseplanet.noisecapture.util.geo.FeatureCollection
import org.noiseplanet.noisecapture.util.geo.GeoJson
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.File

/**
 * File system service using OPFS
 */
@OptIn(ExperimentalWasmJsInterop::class)
class OPFSFileSystemService : FileSystemService {

    // - Public functions

    override suspend fun getFileSize(fileUri: String): Long? {
        val (fileHandle, _) = OPFSHelper.getFileHandle(fileUri) ?: return null
        val file: File = fileHandle.getFile().await()

        return file.size.toInt().toLong()
    }

    override suspend fun deleteFile(fileUri: String) {
        val (fileHandle, directoryHandle) = OPFSHelper.getFileHandle(fileUri) ?: return
        directoryHandle.removeEntry(fileHandle.name).await<Unit>()
    }

    /**
     * Using OPFS, root storage point is just an empty URL.
     */
    override fun getRootDirectory(): String = ""

    override suspend fun downloadFile(fileUri: String) {
        // Get file blob
        val (fileHandle, _) = OPFSHelper.getFileHandle(fileUri) ?: return
        val file: File = fileHandle.getFile().await()

        // Download it
        downloadBlob(file, file.name)
    }

    /**
     * Download files using Zip.js library: https://jsr.io/@zip-js/zip-js
     */
    override suspend fun downloadFiles(fileUris: List<String>, archiveName: String) {
        val writer = ZipJs.BlobWriter("applications/zip")
        val zipWriter = ZipJs.ZipWriter(writer)

        // Add each file to the zip archive
        fileUris.forEach { fileUri ->
            val (fileHandle, _) = OPFSHelper.getFileHandle(fileUri) ?: return
            val file = fileHandle.getFile().await<File>()
            val reader = ZipJs.BlobReader(file)
            zipWriter.add(fileUri, reader).await<JsAny>()
        }
        zipWriter.close().await<JsAny>()

        // Get zipped data as blob
        val blob = writer.getData().await<Blob>()

        // Download zip file
        downloadBlob(blob, "$archiveName.zip")
    }

    override suspend fun downloadGeoJson(geoJson: FeatureCollection, fileName: String) {
        val contents = GeoJson.encodeToString(geoJson)
        // Write data to blob
        val blob = Blob(
            blobParts = arrayOf<JsAny?>(contents.toJsString()).toJsArray(),
            options = BlobPropertyBag(type = "application/geo+json")
        )
        downloadBlob(blob, fileName)
    }

    /**
     * Store a Blob of audio in persistent storage.
     *
     * @param key Unique identifier of the audio file.
     * @param blob Audio data.
     */
    suspend fun store(key: String, blob: Blob) {
        // Get file handle and create a writable stream
        val (fileHandle, _) = OPFSHelper.getFileHandle(key, createIfNotFound = true) ?: return
        val stream: FileSystemWritableFileStream = fileHandle.createWritable().await()
        // Store raw data
        stream.write(blob).await<Unit>()
        // Close stream
        stream.close().await<Unit>()
    }


    // - Private functions

    /**
     * Downloads the given blob to the user's Downloads folder, with the given filename.
     */
    private fun downloadBlob(blob: Blob, fileName: String) {
        // Create download URL
        val url = URL.createObjectURL(blob)

        // Create anchor element and trigger the download
        val anchor = document.createElement("a") as HTMLAnchorElement
        anchor.href = url
        anchor.download = fileName
        document.body?.appendChild(anchor)
        anchor.click()

        // Clean up
        document.body?.removeChild(anchor)
        URL.revokeObjectURL(url)
    }
}
