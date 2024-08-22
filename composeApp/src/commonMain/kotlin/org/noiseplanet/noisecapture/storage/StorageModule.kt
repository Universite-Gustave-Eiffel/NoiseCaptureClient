package org.noiseplanet.noisecapture.storage

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Should be implemented in each platform to use the specific storage service
 */
internal expect fun storageServiceModule(): Module

internal val defaultStorageModule = module {
    single<StorageService> { DefaultStorageService() }
}
