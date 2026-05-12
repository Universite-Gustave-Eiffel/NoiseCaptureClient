package org.noiseplanet.noisecapture.services.storage

import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import org.noiseplanet.noisecapture.util.geo.FeatureCollection


@Suppress("TooManyFunctions")
interface FileSystemService {

    // - Properties

    val dispatcher: CoroutineDispatcher
        get() = Dispatchers.Default


    // - Public functions

    /**
     * Reads bytes from file at given URI.
     *
     * @param fileUri File URI.
     * @return File contents as [ByteArray], null if file doesn't exist.
     */
    suspend fun read(fileUri: String): ByteArray? = with(dispatcher) {
        val path = getAbsolutePath(fileUri) ?: return@with null
        if (!SystemFileSystem.exists(path)) return@with null

        SystemFileSystem.source(path).buffered().readByteArray()
    }

    /**
     * Reads contents of file at given URI as UTF-8 encoded String.
     *
     * @param fileUri File URI.
     * @return File contents as [String], null if file doesn't exist.
     */
    suspend fun readString(fileUri: String): String? = with(dispatcher) {
        return@with read(fileUri)?.decodeToString()
    }

    /**
     * Write given bytes to file at given URI.
     *
     * @param fileUri File URI.
     * @param bytes Bytes to write.
     * @param append If true, appends bytes to previously existing file contents. Defaults to false.
     */
    suspend fun write(fileUri: String, bytes: ByteArray, append: Boolean = false) =
        with(dispatcher) {
            val path = getAbsolutePath(fileUri) ?: return@with
            SystemFileSystem.sink(path, append).buffered().write(bytes)
        }

    /**
     * Write given string to file at given URI.
     *
     * @param fileUri File URI.
     * @param text String to write.
     * @param append If true, appends bytes to previously existing file contents. Defaults to false.
     */
    suspend fun writeString(fileUri: String, text: String, append: Boolean = false) =
        with(dispatcher) {
            write(fileUri, text.toByteArray(), append)
        }

    /**
     * Gets the size of the file at the given URI, in bytes.
     *
     * @param fileUri File URI.
     * @return File size in bytes, null if not found.
     */
    suspend fun size(fileUri: String): Long? = with(dispatcher) {
        val path = getAbsolutePath(fileUri) ?: return@with null

        return@with SystemFileSystem.metadataOrNull(path)?.size
    }

    /**
     * Deletes the file at the given URI, relatively to the root directory.
     *
     * @param fileUri File URI.
     */
    suspend fun delete(fileUri: String) = with(dispatcher) {
        val path = getAbsolutePath(fileUri) ?: return@with

        return@with SystemFileSystem.delete(path, mustExist = false)
    }

    /**
     * Opens download dialog for the given file, based on the current platform.
     *
     * @param fileUri File URI, relative to the root directory.
     */
    suspend fun download(fileUri: String)

    /**
     * Serialises and shows download options for the given GeoJson object.
     *
     * @param geoJson GeoJson object
     * @param fileName Output file name
     */
    suspend fun downloadGeoJson(geoJson: FeatureCollection, fileName: String)

    /**
     * Zips the files at the given URIs into an archive in cache space, then lets the user download
     * this archive through platform dependant [download] function.
     *
     * @param fileUris URIs of the files to download, relative to the root directory.
     * @param archiveName Name of the output archive. Defaults to "NoiseCapture_Export".
     */
    suspend fun download(fileUris: List<String>, archiveName: String = "NoiseCapture_Export")

    /**
     * Returns the URI to the root directory of application files, depending on the current platform.
     */
    fun getRootDirectory(): String?

    /**
     * From a given relative file path on the filesystem, returns the absolute path depending
     * on the current platform.
     *
     * @param relativePath Relative path from root directory.
     * @return Absolute path (including path to root directory).
     */
    fun getAbsolutePath(relativePath: String): Path? {
        val rootDir = getRootDirectory() ?: return null
        return Path("${rootDir.replace(Regex("/*$"), "")}/$relativePath")
    }
}
