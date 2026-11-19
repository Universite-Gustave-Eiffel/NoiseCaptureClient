package org.noiseplanet.noisecapture.service

import io.ktor.client.plugins.cache.storage.CachedResponseData
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.noiseplanet.noisecapture.TestClock
import org.noiseplanet.noisecapture.http.FilesystemHttpCacheStorage
import org.noiseplanet.noisecapture.mock.MockFileSystemService
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds


class FilesystemHttpCacheStorageTest {

    @Test
    fun testStoreFindRemove() = runTest {
        val clock = TestClock(testScheduler)
        val fileSystemService = MockFileSystemService()
        val cacheStorage = FilesystemHttpCacheStorage(
            cacheSystemService = fileSystemService,
            clock = clock
        )
        val url = Url("http://test/resource.json")
        val responseData = CachedResponseData(
            url = url,
            statusCode = HttpStatusCode(value = 200, description = "Success"),
            requestTime = GMTDate(clock.now().toEpochMilliseconds() - 2_000),
            responseTime = GMTDate(clock.now().toEpochMilliseconds() - 1_500),
            version = HttpProtocolVersion.HTTP_2_0,
            expires = GMTDate(clock.now().toEpochMilliseconds() + 2_000),
            headers = HeadersBuilder().build(),
            varyKeys = emptyMap(),
            body = Random.nextBytes(64)
        )

        assertEquals(emptySet(), cacheStorage.findAll(url))

        cacheStorage.store(url, responseData)
        assertEquals(setOf(responseData), cacheStorage.findAll(url))

        cacheStorage.removeAll(url)
        assertEquals(emptySet(), cacheStorage.findAll(url))
    }

    @Test
    fun testExpire() = runTest {
        val clock = TestClock(testScheduler)
        val fileSystemService = MockFileSystemService()
        val cacheStorage = FilesystemHttpCacheStorage(
            cacheSystemService = fileSystemService,
            clock = clock
        )
        val url = Url("http://test/resource.json")
        val responseData = CachedResponseData(
            url = url,
            statusCode = HttpStatusCode(value = 200, description = "Success"),
            requestTime = GMTDate(clock.now().toEpochMilliseconds() - 2_000),
            responseTime = GMTDate(clock.now().toEpochMilliseconds() - 1_500),
            version = HttpProtocolVersion.HTTP_2_0,
            expires = GMTDate(clock.now().toEpochMilliseconds() + 2_000),
            headers = HeadersBuilder().build(),
            varyKeys = emptyMap(),
            body = Random.nextBytes(64)
        )

        assertEquals(emptySet(), cacheStorage.findAll(url))

        cacheStorage.store(url, responseData)
        delay(1.seconds)
        assertEquals(setOf(responseData), cacheStorage.findAll(url))

        // Stored entry should have expired
        delay(3.seconds)
        assertEquals(emptySet(), cacheStorage.findAll(url))

        // Storage should now be empty
        assertEquals(emptyList(), fileSystemService.list())
    }

    @Test
    fun testControlCacheSize() = runTest {
        val clock = TestClock(testScheduler)
        val fileSystemService = MockFileSystemService()
        val cacheStorage = FilesystemHttpCacheStorage(
            cacheSystemService = fileSystemService,
            clock = clock,
            maxCacheSizeBytes = 128,
        )
        val url1 = Url("http://test/resource_1.json")
        val responseData1 = CachedResponseData(
            url = url1,
            statusCode = HttpStatusCode(value = 200, description = "Success"),
            requestTime = GMTDate(clock.now().toEpochMilliseconds() - 2_000),
            responseTime = GMTDate(clock.now().toEpochMilliseconds() - 1_500),
            version = HttpProtocolVersion.HTTP_2_0,
            expires = GMTDate(clock.now().toEpochMilliseconds() + 1_000),
            headers = HeadersBuilder().build(),
            varyKeys = emptyMap(),
            body = Random.nextBytes(64),
        )
        val url2 = Url("http://test/resource_2.json")
        val responseData2 = CachedResponseData(
            url = url2,
            statusCode = HttpStatusCode(value = 200, description = "Success"),
            requestTime = GMTDate(clock.now().toEpochMilliseconds() - 2_000),
            responseTime = GMTDate(clock.now().toEpochMilliseconds() - 1_500),
            version = HttpProtocolVersion.HTTP_2_0,
            expires = GMTDate(clock.now().toEpochMilliseconds() + 2_000),
            headers = HeadersBuilder().build(),
            varyKeys = emptyMap(),
            body = Random.nextBytes(64),
        )
        val url3 = Url("http://test/resource_3.json")
        val responseData3 = CachedResponseData(
            url = url1,
            statusCode = HttpStatusCode(value = 200, description = "Success"),
            requestTime = GMTDate(clock.now().toEpochMilliseconds() - 2_000),
            responseTime = GMTDate(clock.now().toEpochMilliseconds() - 1_500),
            version = HttpProtocolVersion.HTTP_2_0,
            expires = GMTDate(clock.now().toEpochMilliseconds() + 3_000),
            headers = HeadersBuilder().build(),
            varyKeys = emptyMap(),
            body = Random.nextBytes(64),
        )

        assertEquals(emptySet(), cacheStorage.findAll(url1))
        assertEquals(emptySet(), cacheStorage.findAll(url2))
        assertEquals(emptySet(), cacheStorage.findAll(url3))

        // Max cache size is 128 bytes and only counts body size (64 each), so two entries should fit
        cacheStorage.store(url1, responseData1)
        cacheStorage.store(url2, responseData2)
        assertEquals(setOf(responseData1), cacheStorage.findAll(url1))
        assertEquals(setOf(responseData2), cacheStorage.findAll(url2))

        // Adding a third entry should exceed cache size, so entry 1 should be removed (expires first)
        cacheStorage.store(url3, responseData3)
        assertEquals(emptySet(), cacheStorage.findAll(url1))
        assertEquals(setOf(responseData2), cacheStorage.findAll(url2))
        assertEquals(setOf(responseData3), cacheStorage.findAll(url3))
    }

    @Test
    fun testCorruptedMetadata() = runTest {
        val clock = TestClock(testScheduler)
        val fileSystemService = MockFileSystemService()

        fileSystemService.writeString("_corrupted.meta", "Invalid metadata")
        fileSystemService.writeString("_corrupted", "Associated body")

        // Upon init, cache storage should clear the corrupted entry
        val cacheStorage = FilesystemHttpCacheStorage(
            cacheSystemService = fileSystemService,
            clock = clock,
            maxCacheSizeBytes = 128,
            scope = backgroundScope
        )

        delay(1.seconds)
        assertEquals(emptyList(), fileSystemService.list())

        val url = Url("corrupted")
        val responseData = CachedResponseData(
            url = url,
            statusCode = HttpStatusCode(value = 200, description = "Success"),
            requestTime = GMTDate(clock.now().toEpochMilliseconds() - 2_000),
            responseTime = GMTDate(clock.now().toEpochMilliseconds() - 1_500),
            version = HttpProtocolVersion.HTTP_2_0,
            expires = GMTDate(clock.now().toEpochMilliseconds() + 2_000),
            headers = HeadersBuilder().build(),
            varyKeys = emptyMap(),
            body = Random.nextBytes(64)
        )
        cacheStorage.store(url, responseData)
        assertEquals(setOf(responseData), cacheStorage.findAll(url))

        // Override the metadata with corrupted data
        fileSystemService.writeString("_corrupted.meta", "Invalid metadata")

        // Trying to access the resource should clear it from cache
        assertEquals(emptySet(), cacheStorage.findAll(url))
        assertEquals(emptyList(), fileSystemService.list())
    }
}
