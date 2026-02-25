package org.noiseplanet.noisecapture.util


/**
 * Returns a string representation of this number, prefixed by a plus sign if greater
 * or equal to zero, or a minus sign if negative.
 */
fun Double.toSignedString(): String {
    return (if (this >= 0) "+" else "") + this.toString()
}
