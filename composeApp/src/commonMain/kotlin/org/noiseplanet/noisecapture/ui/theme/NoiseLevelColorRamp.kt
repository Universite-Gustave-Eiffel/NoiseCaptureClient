package org.noiseplanet.noisecapture.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * SPL color representation based on
 * [this palette](https://noisemodelling.readthedocs.io/en/latest/Noise_Map_Color_Scheme.html#coloring-noise)
 */
object NoiseLevelColorRamp {

    // - Properties

    val palette: Map<Double, Color> = mapOf(
        0.0 to Color(0xFF82A6AD),
        35.0 to Color(0xFFA0BABF),
        40.0 to Color(0xFFB8D6D1),
        45.0 to Color(0xFFCEE4CC),
        50.0 to Color(0xFFECDFA0),
        55.0 to Color(0xFFF3C683),
        60.0 to Color(0xFFE87E4D),
        65.0 to Color(0xFFCD463E),
        70.0 to Color(0xFFA11A4D),
        75.0 to Color(0xFF75085C),
        80.0 to Color(0xFF430A4A),
    )


    // - Public functions

    /**
     * Get the color corresponding to the given SPL value
     *
     * @param value SPL value
     *
     * @return Corresponding color from palette
     */
    fun getColorForSPLValue(value: Double): Color {
        return palette.filter { it.key <= value }
            .minByOrNull { value - it.key }
            ?.value
            ?: Color.Black
    }
}
