package org.noiseplanet.noisecapture.storage

import io.github.xxfast.kstore.Codec
import kotlinx.serialization.Serializable

class DefaultStorageCodec<T : @Serializable Any>(
    private var stored: T?,
    private val delay: (suspend (value: T?) -> Unit)? = null,
) : Codec<T> {
    override suspend fun encode(value: T?) {
        delay?.invoke(value)
        stored = value
    }

    override suspend fun decode(): T? {
        val value: T? = stored as? T
        delay?.invoke(value)
        return value
    }
}
