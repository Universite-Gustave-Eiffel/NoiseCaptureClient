package org.noiseplanet.noisecapture.audio.signal.window

data class Window(val epoch: Long, val samples: FloatArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Window

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
