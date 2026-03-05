package org.noiseplanet.noisecapture.audio.signal.window

data class Window(val timestamp: Long, val samples: FloatArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Window

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
