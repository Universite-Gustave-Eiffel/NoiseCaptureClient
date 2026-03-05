package org.noiseplanet.noisecapture.util


/**
 * Returns a string representation of this number, prefixed by a plus sign if greater
 * or equal to zero, or a minus sign if negative.
 */
fun Double.toSignedString(): String {
    return (if (this >= 0) "+" else "") + this.toString()
}

/**
 * Slugifies this string, removing all non a-z 0-9 characters and replacing
 * groups of whitespaces with dashes.
 */
fun String.slugify(): String = this
    .replace("[^\\p{ASCII}]".toRegex(), "")
    .replace("[^a-zA-Z0-9\\s]+".toRegex(), "").trim()
    .replace("\\s+".toRegex(), "-")
    .lowercase()
