package org.noiseplanet.noisecapture.services.storage

interface FileSystemService {

    /**
     * Gets the size of the file at the given URI, in bytes.
     *
     * @param fileUri File URI.
     * @return File size in bytes, null if not found.
     */
    fun getFileSize(fileUri: String): Long?

    /**
     * Deletes the file at the given URI.
     *
     * @param fileUri File URI.
     */
    fun deleteFile(fileUri: String)

    /**
     * Returns the absolute path to the directory containing audio recordings.
     *
     * @return Absolute path to the directory containing audio recordings.
     */
    fun getAudioFilesDirectoryUri(): String?

    /**
     * Returns the absolute path for the given audio file, depending on the current platform.
     *
     * @param fileName Audio file name.
     * @return Absolute file URI, or null in case of error.
     */
    fun getAudioFileAbsolutePath(fileName: String): String? {
        val rootDir = getAudioFilesDirectoryUri() ?: return null

        // Append file name to enclosing directory path (after removing any trailing slashes)
        return "${rootDir.replace(Regex("/*$"), "")}/$fileName"
    }
}
