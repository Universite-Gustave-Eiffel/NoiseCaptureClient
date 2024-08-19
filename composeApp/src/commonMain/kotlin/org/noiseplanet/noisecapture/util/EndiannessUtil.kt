package org.noiseplanet.noisecapture.util

/**
 * Convert Int into little endian array of bytes
 *
 * // TODO: Add unit tests
 */
fun Int.toLittleEndianBytes(): ByteArray = byteArrayOf(
    this.toByte(),
    this.ushr(8).toByte(),
    this.ushr(16).toByte(),
    this.ushr(24).toByte()
)
