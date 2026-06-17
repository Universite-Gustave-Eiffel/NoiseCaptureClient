package org.noiseplanet.noisecapture.http

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.cache.HttpCache
import org.koin.dsl.module


val httpModule = module {

    single<HttpClient> {
        HttpClient {
            install(HttpCache) {
                val cacheStorage = FilesystemHttpCacheStorage()
                this.privateStorage(cacheStorage)
                this.publicStorage(cacheStorage)
            }

            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 5)
                exponentialDelay()
            }
        }
    }
}
