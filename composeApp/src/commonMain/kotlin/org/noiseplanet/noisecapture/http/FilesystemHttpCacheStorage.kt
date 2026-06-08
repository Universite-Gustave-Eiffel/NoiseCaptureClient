package org.noiseplanet.noisecapture.http

import io.ktor.client.plugins.cache.storage.CacheStorage
import io.ktor.client.plugins.cache.storage.CachedResponseData
import io.ktor.http.Url
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import nl.jacobras.humanreadable.HumanReadable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.services.storage.CacheFileSystemService
import org.noiseplanet.noisecapture.util.injectLogger


class FilesystemHttpCacheStorage : CacheStorage, KoinComponent {

    // - Constants

    companion object {

        const val MAX_CACHE_SIZE_BYTES: Long = 25 * 1024 * 1024 // 25MB
    }


    // - Properties

    private val logger: Logger by injectLogger()
    private val cacheSystemService: CacheFileSystemService by inject()

    private var entries: MutableMap<String, CachedResponseMetadata>? = null

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())


    // - Lifecycle

    init {
        scope.launch { controlCacheSize() }
    }


    // - Public functions

    override suspend fun store(
        url: Url,
        data: CachedResponseData,
    ) {
        val cacheKey = cacheKeyFromUrl(url)
        val metadataKey = "$cacheKey.meta"
        val metadata = data.metadata()

        // Write response to cache storage
        cacheSystemService.write(fileUri = cacheKey, bytes = data.body)

        // Write metadata
        cacheSystemService.writeString(
            fileUri = metadataKey,
            text = Json.encodeToString(metadata)
        )

        // Store metadata in in-memory index
        entries?.set(cacheKey, metadata)

        // Check cache size and remove oldest entries if needed
        controlCacheSize()
    }

    override suspend fun find(
        url: Url,
        varyKeys: Map<String, String>,
    ): CachedResponseData? {
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
        entries?.remove(cacheKey)
    }

    override suspend fun removeAll(url: Url) {
        logger.info("CACHE REMOVE ALL")
        remove(url, emptyMap())
    }


    // - Private functions

    private fun cacheKeyFromUrl(url: Url): String {
        return url.encodedPath.replace("/", "_")
    }

    private suspend fun controlCacheSize() {
        // The first time we control cache size, read all current metadata files
        if (entries == null) {
            val metaDataPaths = cacheSystemService.list().filter { it.endsWith(".meta") }
            entries = mutableMapOf()

            for (path in metaDataPaths) {
                val metadata: CachedResponseMetadata = cacheSystemService.readString(path)
                    ?.let { Json.decodeFromString(it) }
                    ?: continue
                entries?.set(cacheKeyFromUrl(metadata.url), metadata)
            }
        }

        // Calculate cache size from entries
        var totalCacheSize = 0L
        entries?.values?.sortedByDescending { it.expires }?.forEach { metadata ->
            totalCacheSize += metadata.bodySize

            // If cache size is over limit, delete entries that will expire first
            if (totalCacheSize > MAX_CACHE_SIZE_BYTES) {
                removeAll(metadata.url)
            }
        }

        logger.debug(
            "CACHE SIZE: ${HumanReadable.fileSize(totalCacheSize)} / ${
                HumanReadable.fileSize(
                    MAX_CACHE_SIZE_BYTES
                )
            }"
        )
    }
}
