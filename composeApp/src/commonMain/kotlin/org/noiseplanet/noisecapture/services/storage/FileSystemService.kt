package org.noiseplanet.noisecapture.services.storage

import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray


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
    suspend fun read(fileUri: String): ByteArray? = withContext(dispatcher) {
        val path = getAbsolutePath(fileUri) ?: return@withContext null
        if (!SystemFileSystem.exists(path)) return@withContext null

        runCatching {
            SystemFileSystem.source(path).buffered().use { it.readByteArray() }
        }.getOrNull()
    }

    /**
     * Reads contents of file at given URI as UTF-8 encoded String.
     *
     * @param fileUri File URI.
     * @return File contents as [String], null if file doesn't exist.
     */
    suspend fun readString(fileUri: String): String? {
        return read(fileUri)?.decodeToString()
    }

    /**
     * Write given bytes to file at given URI.
     *
     * @param fileUri File URI.
     * @param bytes Bytes to write.
     * @param append If true, appends bytes to previously existing file contents. Defaults to false.
     */
    suspend fun write(fileUri: String, bytes: ByteArray, append: Boolean = false) =
        withContext(dispatcher) {
            val path = getAbsolutePath(fileUri) ?: return@withContext
            path.parent?.let { SystemFileSystem.createDirectories(it) }

            SystemFileSystem.sink(path, append)
                .buffered()
                .use { sink -> sink.write(bytes) }
        }

    /**
     * Write given string to file at given URI.
     *
     * @param fileUri File URI.
     * @param text String to write.
     * @param append If true, appends bytes to previously existing file contents. Defaults to false.
     */
    suspend fun writeString(fileUri: String, text: String, append: Boolean = false) {
        write(fileUri, text.toByteArray(), append)
    }

    /**
     * Gets the size of the file at the given URI, in bytes.
     *
     * @param fileUri File URI.
     * @return File size in bytes, null if not found.
     */
    suspend fun size(fileUri: String): Long? = withContext(dispatcher) {
        val path = getAbsolutePath(fileUri) ?: return@withContext null

        SystemFileSystem.metadataOrNull(path)?.size
    }

    /**
     * Deletes the file at the given URI, relatively to the root directory.
     *
     * @param fileUri File URI.
     */
    suspend fun delete(fileUri: String) = withContext(dispatcher) {
        val path = getAbsolutePath(fileUri) ?: return@withContext

        SystemFileSystem.delete(path, mustExist = false)
    }

    /**
     * True if the file at the given URI exists, false otherwise.
     *
     * @param fileUri File URI.
     * @return True if the file at the given URI exists, false otherwise.
     */
    suspend fun exists(fileUri: String): Boolean = withContext(dispatcher) {
        val path = getAbsolutePath(fileUri) ?: return@withContext false

        SystemFileSystem.exists(path)
    }

    /**
     * Lists contents found at given URI.
     *
     * @param uri Directory URI
     * @return URIs of directory children
     */
    suspend fun list(uri: String = ""): List<String> = withContext(dispatcher) {
        val path = getAbsolutePath(uri) ?: return@withContext emptyList()
        val root = getRootDirectory().toString()

        SystemFileSystem.list(path).map { path ->
            path.toString().replaceFirst(root, "")
        }
    }

    /**
     * Opens download dialog for the given file, based on the current platform.
     *
     * @param fileUri File URI, relative to the root directory.
     */
    suspend fun download(fileUri: String)

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
