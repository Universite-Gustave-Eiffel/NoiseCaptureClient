package org.noiseplanet.noisecapture.storage

import io.github.xxfast.kstore.Codec
import kotlinx.serialization.Serializable

class DefaultStorageCodec<T : @Serializable Any>(
    private val storage : MutableMap<String, @Serializable Any>,
    private val documentId: String,
    private val delay: (suspend (value: T?) -> Unit)? = null,
) : Codec<T> {

    override suspend fun encode(value: T?) {
        delay?.invoke(value)
        storage[documentId] = value as @Serializable Any
    }

    override suspend fun decode(): T? {
        val value: T? = storage[documentId] as? T
        delay?.invoke(value)
        return value
    }
}