package org.noiseplanet.noisecapture.util

import kotlin.random.Random
import kotlin.random.nextUBytes

class UUIDGenerator {

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun createV4UUID() : String {
            var timeHigh = IntArray(4) { Random.nextInt(256) }
            var timeLow = IntArray(2) { Random.nextInt(256) }
            var reserved = IntArray(2) { Random.nextInt(256) }
            var family = IntArray(2) { Random.nextInt(256) }
            var node = IntArray(6) { Random.nextInt(256) }
            // Fix version byte
            reserved[0] = (0x40 or (0x0F and reserved[0]))
            // Fix variant bits (variant 1, from 0x80 to 0xBF)
            family[0] = ((0b10 shl 6) or (family[0] shr 2))

            return timeHigh.joinToString(separator = "") { it.toByte().toHexString() } +
                    "-" +
                    timeLow.joinToString(separator = "") { it.toByte().toHexString() } +
                    "-" +
                    reserved.joinToString(separator = "") { it.toByte().toHexString() } +
                    "-" +
                    family.joinToString(separator = "") { it.toByte().toHexString() } +
                    "-" +
                    node.joinToString(separator = "") { it.toByte().toHexString() }
        }
    }
}