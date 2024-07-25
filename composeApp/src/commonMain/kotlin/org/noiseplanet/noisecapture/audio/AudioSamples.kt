package org.noiseplanet.noisecapture.audio

data class AudioSamples(
    val epoch: Long,
    val samples: FloatArray,
    val sampleRate: Int,
    val errorCode: ErrorCode? = null,
) {

    enum class ErrorCode {
        ABORTED,
        DEVICE_ERROR
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AudioSamples

        if (epoch != other.epoch) return false
        if (!samples.contentEquals(other.samples)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = epoch.hashCode()
        result = 31 * result + samples.contentHashCode()
        return result
    }
}
