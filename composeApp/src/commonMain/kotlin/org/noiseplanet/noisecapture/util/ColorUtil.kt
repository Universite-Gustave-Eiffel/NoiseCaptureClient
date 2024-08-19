package org.noiseplanet.noisecapture.util

import androidx.compose.ui.graphics.Color

/**
 * Parses the given color string as an ARGB color integer
 *
 * TODO: Add unit tests
 *
 * @param colorString Input color string
 * @return ARGB color integer
 */
fun parseColor(colorString: String): Int {
    var color = colorString.substring(1).toLong(16)
    if (colorString.length == 7) {
        // Set the alpha value
        color = color or 0x00000000ff000000L
    } else {
        require(colorString.length != 9) { "Unknown color" }
    }
    return color.toInt()
}

/**
 * Tries to interpret this string as a compose Color.
 *
 * TODO: Add Unit tests
 *
 * @return Parsed [Color] object
 */
fun String.toComposeColor(): Color {
    return Color(parseColor(this))
}
