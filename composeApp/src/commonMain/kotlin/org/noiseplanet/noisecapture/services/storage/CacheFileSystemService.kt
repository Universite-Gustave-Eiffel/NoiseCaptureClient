package org.noiseplanet.noisecapture.services.storage

import kotlinx.io.files.SystemFileSystem


/**
 * Child interface of [FileSystemService] to be used to interact with files in cache storage
 * instead of application storage.
 */
interface CacheFileSystemService : FileSystemService {

    /**
     * Gets the current size of all currently cached files in bytes.
     *
     * @return Current cache size in bytes.
     */
    suspend fun getTotalCacheSize(): Long? = with(dispatcher) {
        getDirSize("")
    }

    /**
     * Gets the size of a directory in bytes.
     *
     * @param directoryUri Path to the directory.
     * @return Size in bytes or null if not found.
     */
    suspend fun getDirSize(directoryUri: String): Long? = with(dispatcher) {
        val path = getAbsolutePath(directoryUri) ?: return@with null
        val rootDir = getRootDirectory() ?: return@with null

        SystemFileSystem.list(path)
            .fold(0L) { acc, path ->
                val metadata = SystemFileSystem.metadataOrNull(path) ?: return@fold acc

                if (metadata.isRegularFile) {
                    acc + metadata.size
                } else {
                    acc + (getDirSize(path.toString().replaceFirst(rootDir, "")) ?: 0)
                }
            }
    }
}
