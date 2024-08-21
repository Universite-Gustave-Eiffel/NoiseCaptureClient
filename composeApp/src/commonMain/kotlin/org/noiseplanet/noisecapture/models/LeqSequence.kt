package org.noiseplanet.noisecapture.models

import kotlinx.serialization.Serializable

@Serializable
data class LeqSequence (
    val leqUtc: Long,
    val lzeq: FloatArray,
    val laeq: FloatArray,
    val lceq: FloatArray,
    val leq50: FloatArray,
    val leq63: FloatArray,
    val leq80: FloatArray,
    val leq100: FloatArray,
    val leq125: FloatArray,
    val leq160: FloatArray,
    val leq200: FloatArray,
    val leq250: FloatArray,
    val leq315: FloatArray,
    val leq400: FloatArray,
    val leq500: FloatArray,
    val leq630: FloatArray,
    val leq800: FloatArray,
    val leq1000: FloatArray,
    val leq1250: FloatArray,
    val leq1600: FloatArray,
    val leq2000: FloatArray,
    val leq2500: FloatArray,
    val leq3150: FloatArray,
    val leq4000: FloatArray,
    val leq5000: FloatArray,
    val leq6300: FloatArray,
    val leq8000: FloatArray,
    val leq10000: FloatArray,
    val leq12500: FloatArray,
    val leq16000: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as LeqSequence

        return leqUtc == other.leqUtc
    }

    override fun hashCode(): Int {
        return leqUtc.hashCode()
    }
}