package org.noiseplanet.noisecapture.http

import io.ktor.client.plugins.cache.storage.CachedResponseData
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import io.ktor.util.date.GMTDate
import kotlinx.serialization.Serializable

/**
 * Serializable copy of [CachedResponseData] (except for the [ByteArray] body) for persistence.
 *
 * Uses serializable types (e.g. [HttpStatusCodeCopy], [HttpProtocolVersionCopy]) so the response
 * can be encoded/decoded across platforms.
 *
 * @property url The request URL.
 * @property statusCode HTTP status code copy.
 * @property requestTime Time of the original request.
 * @property responseTime Time of the cached response.
 * @property version HTTP protocol version copy.
 * @property expires Expiration time of the cached entry.
 * @property headers Response headers.
 * @property varyKeys Vary keys for content negotiation.
 */
@Serializable
internal data class CachedResponseMetadata(
    val url: Url,
    val statusCode: HttpStatusCodeCopy,
    val requestTime: GMTDate,
    val responseTime: GMTDate,
    val version: HttpProtocolVersionCopy,
    val expires: GMTDate,
    val headers: Map<String, List<String>>,
    val varyKeys: Map<String, String>,
    val bodySize: Long,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CachedResponseMetadata) return false

        if (url != other.url) return false
        if (varyKeys != other.varyKeys) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + varyKeys.hashCode()
        return result
    }
}

/**
 * Serializable copy of [io.ktor.http.HttpStatusCode] for persistence.
 *
 * @property value HTTP status code value (e.g. 200, 404).
 * @property description Status description (e.g. "OK", "Not Found").
 */
@Serializable
internal data class HttpStatusCodeCopy(val value: Int, val description: String)

/**
 * Serializable copy of [io.ktor.http.HttpProtocolVersion] for persistence.
 *
 * @property name Protocol name (e.g. "HTTP").
 * @property major Major version number.
 * @property minor Minor version number.
 */
@Serializable
internal data class HttpProtocolVersionCopy(val name: String, val major: Int, val minor: Int)

internal fun CachedResponseData.metadata(): CachedResponseMetadata {
    val httpStatusCodeCopy = HttpStatusCodeCopy(
        value = statusCode.value,
        description = statusCode.description
    )
    val httpProtocolVersionCopy = HttpProtocolVersionCopy(
        name = version.name,
        major = version.major,
        minor = version.minor
    )
    return CachedResponseMetadata(
        url = url,
        statusCode = httpStatusCodeCopy,
        requestTime = requestTime,
        responseTime = responseTime,
        headers = headers.entries().associate { it.key to it.value },
        varyKeys = varyKeys,
        version = httpProtocolVersionCopy,
        expires = expires,
        bodySize = body.size.toLong()
    )
}

internal fun CachedResponseMetadata.withBody(body: ByteArray): CachedResponseData {
    val httpStatusCode = HttpStatusCode(
        value = statusCode.value,
        description = statusCode.description
    )
    val httpProtocolVersion = HttpProtocolVersion(
        name = version.name,
        major = version.major,
        minor = version.minor
    )
    return CachedResponseData(
        url = url,
        statusCode = httpStatusCode,
        requestTime = requestTime,
        responseTime = responseTime,
        headers = headersOf(*headers.map { it.key to it.value }.toTypedArray()),
        varyKeys = varyKeys,
        body = body,
        version = httpProtocolVersion,
        expires = expires
    )
}
