package org.noiseplanet.noisecapture.util

import kotlin.random.Random
import kotlin.random.nextUBytes

class UUIDGenerator {

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun createV4UUID(random: Random) : String {
            val timeHigh = IntArray(4) { random.nextInt(256) }
            val timeLow = IntArray(2) { random.nextInt(256) }
            val reserved = IntArray(2) { random.nextInt(256) }
            val family = IntArray(2) { random.nextInt(256) }
            val node = IntArray(6) { random.nextInt(256) }
            // Fix version byte
            reserved[0] = (0x40 or (0x0F and reserved[0]))
            // Fix variant bits (variant 1, from 0x80 to 0xBF)
            family[0] = ((0b10 shl 6) or (family[0] shr 2))

            return buildString {
                append(timeHigh.joinToString(separator = "") { it.toByte().toHexString() })
                append("-")
                append(timeLow.joinToString(separator = "") { it.toByte().toHexString() })
                append("-")
                append(reserved.joinToString(separator = "") { it.toByte().toHexString() })
                append("-")
                append(family.joinToString(separator = "") { it.toByte().toHexString() })
                append("-")
                append(node.joinToString(separator = "") { it.toByte().toHexString() })
            }
        }
    }
}