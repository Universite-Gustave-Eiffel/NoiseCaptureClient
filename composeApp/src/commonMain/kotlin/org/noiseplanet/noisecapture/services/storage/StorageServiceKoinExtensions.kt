package org.noiseplanet.noisecapture.services.storage

import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.definition.Definition
import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module
import org.koin.core.qualifier.named

/**
 * Utility function to inject a storage service for a generic type.
 *
 * Koin itself cannot handle injection of generic types
 * ([see documentation](https://insert-koin.io/docs/reference/koin-core/definitions/#dealing-with-generics))
 * we must name our services with a string.
 *
 * @param T Record type.
 */
inline fun <reified T : @Serializable Any> KoinComponent.injectStorageService(): Lazy<StorageService<T>> {
    return inject(named(StorageService.className<T>()))
}

/**
 * Utility function to register a single storage service instance for a generic type.
 *
 * Koin itself cannot handle injection of generic types
 * ([see documentation](https://insert-koin.io/docs/reference/koin-core/definitions/#dealing-with-generics))
 * we must name our services with a string.
 *
 * @param definition Storage service definition
 * @param T Record type.
 */
inline fun <reified T : @Serializable Any> Module.singleStorageService(
    noinline definition: Definition<StorageService<T>>,
): KoinDefinition<StorageService<T>> {
    return single(
        qualifier = named(StorageService.className<T>()),
        definition = definition
    )
}
