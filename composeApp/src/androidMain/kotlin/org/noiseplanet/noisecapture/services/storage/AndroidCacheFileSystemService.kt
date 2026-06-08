package org.noiseplanet.noisecapture.services.storage


class AndroidCacheFileSystemService : AndroidFileSystemService(), CacheFileSystemService {

    override fun getRootDirectory(): String {
        return context.cacheDir.absolutePath
    }
}
