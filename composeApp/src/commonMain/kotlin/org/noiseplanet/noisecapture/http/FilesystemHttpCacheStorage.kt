package org.noiseplanet.noisecapture.http

import io.ktor.client.plugins.cache.storage.CacheStorage
import io.ktor.client.plugins.cache.storage.CachedResponseData
import io.ktor.http.Url
import io.ktor.util.date.GMTDate
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.services.storage.CacheFileSystemService
import org.noiseplanet.noisecapture.util.injectLogger


class FilesystemHttpCacheStorage(

) : CacheStorage, KoinComponent {

    // - Properties

    private val logger: Logger by injectLogger()
    private val cacheSystemService: CacheFileSystemService by inject()


    // - Public functions

    override suspend fun store(
        url: Url,
        data: CachedResponseData,
    ) {
        logger.info("CACHE STORE 🦫")
        val cacheKey = cacheKeyFromUrl(url)
        val metadataKey = "$cacheKey.meta"

        // Write response to cache storage
        cacheSystemService.write(fileUri = cacheKey, bytes = data.body)

        // Write metadata
        cacheSystemService.writeString(
            fileUri = metadataKey,
            text = Json.encodeToString(data.metadata())
        )

        // TODO: Check cache size and remove entries if needed
//        enforceCacheSize()
    }

    override suspend fun find(
        url: Url,
        varyKeys: Map<String, String>,
    ): CachedResponseData? {
//        logger.info("CACHE FIND")
        val cacheKey = cacheKeyFromUrl(url)
        val metadataKey = "$cacheKey.meta"

        // If entry (or associated metadata) not found, return null
        if (!cacheSystemService.exists(cacheKey) || !cacheSystemService.exists(metadataKey)) {
            return null
        }
        // Try to read cached value's expiration time
        val metadata: CachedResponseMetadata = cacheSystemService.readString(metadataKey)
            ?.let { Json.decodeFromString(it) }
            ?: return null
        // If cached data has expired, delete it
        if (GMTDate() > metadata.expires) {
            remove(url, varyKeys)
            return null
        }

        // Return cached response
        val body = cacheSystemService.read(cacheKey) ?: return null
        return metadata.withBody(body)
    }

    override suspend fun findAll(url: Url): Set<CachedResponseData> {
//        logger.info("CACHE FIND ALL")
        return find(url, emptyMap())
            ?.let { setOf(it) }
            ?: emptySet()
    }

    override suspend fun remove(
        url: Url,
        varyKeys: Map<String, String>,
    ) {
        logger.info("CACHE REMOVE")
        val cacheKey = cacheKeyFromUrl(url)
        val metadataKey = "$cacheKey.meta"
        cacheSystemService.delete(cacheKey)
        cacheSystemService.delete(metadataKey)
    }

    override suspend fun removeAll(url: Url) {
        logger.info("CACHE REMOVE ALL")
        remove(url, emptyMap())
    }


    // - Private functions

    private fun cacheKeyFromUrl(url: Url): String {
        return url.encodedPath.replace("/", "_")
    }
}
