package org.noiseplanet.noisecapture.audio

/**
 * A set of audio samples taken at a given timestamp, at a given sample rate.
 *
 * @param timestamp Timestamp of the last sample, in milliseconds since epoch.
 * @param samples Samples array (PCM float).
 * @param sampleRate Sample rate at which samples were taken.
 */
data class AudioSamples(
    val timestamp: Long,
    val samples: FloatArray,
    val sampleRate: Int,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AudioSamples

        if (timestamp != other.timestamp) return false
        if (!samples.contentEquals(other.samples)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + samples.contentHashCode()
        return result
    }
}
