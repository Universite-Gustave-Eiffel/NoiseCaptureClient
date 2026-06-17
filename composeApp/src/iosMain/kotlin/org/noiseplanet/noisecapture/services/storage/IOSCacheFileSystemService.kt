package org.noiseplanet.noisecapture.services.storage

import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask


open class IOSCacheFileSystemService : IOSFileSystemService(), CacheFileSystemService {

    override fun getRootDirectory(): String? {
        val urls = fileManager.URLsForDirectory(
            directory = NSCachesDirectory,
            inDomains = NSUserDomainMask
        )
        val url = urls.firstOrNull() as? NSURL? ?: return null
        return url.path
    }
}
