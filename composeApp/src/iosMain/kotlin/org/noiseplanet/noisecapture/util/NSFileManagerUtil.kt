package org.noiseplanet.noisecapture.util

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ptr
import platform.Foundation.NSFileManager


/**
 * Utility function to create directory at given path, including any non existing intermediary
 * directories.
 *
 * @param path Path to directory to create.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun NSFileManager.createDirectoriesAtPath(path: String) {
    // Create enclosing directories if they doesn't exist
    runCatchingNSError { nsError ->
        createDirectoryAtPath(
            path = path,
            attributes = null,
            withIntermediateDirectories = true,
            error = nsError.ptr
        )
    }
}
