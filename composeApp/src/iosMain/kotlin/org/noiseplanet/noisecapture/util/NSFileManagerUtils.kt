package org.noiseplanet.noisecapture.util

import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * Utility functions to access directories using [NSFileManager]
 */
object NSFileManagerUtils {

    /**
     * Gets an [NSURL] pointing to the app's documents directory.
     *
     * @return URL to documents directory or nil if not found or not accessible.
     */
    fun getDocumentsDirectory(): NSURL? {
        val urls = NSFileManager.defaultManager.URLsForDirectory(
            directory = NSDocumentDirectory,
            inDomains = NSUserDomainMask
        )
        return urls.firstOrNull() as? NSURL?
    }
}
