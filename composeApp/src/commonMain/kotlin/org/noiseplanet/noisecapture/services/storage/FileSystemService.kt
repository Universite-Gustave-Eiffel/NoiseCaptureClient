package org.noiseplanet.noisecapture.services.storage

import org.noiseplanet.noisecapture.util.FeatureCollection


interface FileSystemService {

    /**
     * Gets the size of the file at the given URI, in bytes.
     *
     * @param fileUri File URI.
     * @return File size in bytes, null if not found.
     */
    suspend fun getFileSize(fileUri: String): Long?

    /**
     * Deletes the file at the given URI, relatively to the root directory.
     *
     * @param fileUri File URI.
     */
    suspend fun deleteFile(fileUri: String)

    /**
     * Opens download dialog for the given file, based on the current platform.
     *
     * @param fileUri File URI, relative to the root directory.
     */
    suspend fun downloadFile(fileUri: String)

    /**
     * Serialises and shows download options for the given GeoJson object.
     *
     * @param geoJson GeoJson object
     * @param fileName Output file name
     */
    suspend fun downloadGeoJson(geoJson: FeatureCollection, fileName: String)

    /**
     * Zips the files at the given URIs into an archive in cache space, then lets the user download
     * this archive through platform dependant [downloadFile] function.
     *
     * @param fileUris URIs of the files to download, relative to the root directory.
     * @param archiveName Name of the output archive. Defaults to "NoiseCapture_Export".
     */
    suspend fun downloadFiles(fileUris: List<String>, archiveName: String = "NoiseCapture_Export")

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
    fun getAbsolutePath(relativePath: String): String? {
        val rootDir = getRootDirectory() ?: return null
        return "${rootDir.replace(Regex("/*$"), "")}/$relativePath"
    }
}
