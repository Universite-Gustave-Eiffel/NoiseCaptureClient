package org.noiseplanet.noisecapture.ui.theme

import androidx.compose.ui.graphics.Color
import org.noiseplanet.noisecapture.util.VuMeterOptions

/**
 * SPL color representation based on [this palette](https://www.coloringnoise.com/).
 */
object NoiseLevelColorRamp {

    // - Properties

    /**
     * Base color palette with true colors picked from [Coloring Noise](https://www.coloringnoise.com/)
     */
    val palette: Map<Double, Color> = mapOf(
        0.0 to Color(0xFF82A6AD),
        35.0 to Color(0xFFA0BABF),
        40.0 to Color(0xFFB8D6D1),
        45.0 to Color(0xFFCEE4CC),
        50.0 to Color(0xFFE2F2BF),
        55.0 to Color(0xFFF3C683),
        60.0 to Color(0xFFE87E4D),
        65.0 to Color(0xFFCD463E),
        70.0 to Color(0xFFA11A4D),
        75.0 to Color(0xFF75085C),
        80.0 to Color(0xFF430A4A),
    )

    /**
     * A darker version of the color palette to provide better contrast against light backgrounds
     */
    val paletteDarker: Map<Double, Color> = mapOf(
        0.0 to Color(0xFF576F73),
        35.0 to Color(0xFF6B7C7F),
        40.0 to Color(0xFF7B8F8B),
        45.0 to Color(0xFF899888),
        50.0 to Color(0xFF97A17F),
        55.0 to Color(0xFFA28457),
        60.0 to Color(0xFFC16940),
        65.0 to Color(0xFFCD463E),
        70.0 to Color(0xFFA11A4D),
        75.0 to Color(0xFF75085C),
        80.0 to Color(0xFF430A4A),
    )

    /**
     * A lighter version of the color palette to use as background tint.
     */
    val paletteLighter: Map<Double, Color> = mapOf(
        0.0 to Color(0xFFE6EDEF),
        35.0 to Color(0xFFECF1F2),
        40.0 to Color(0xFFF1F7F6),
        45.0 to Color(0xFFF5FAF5),
        50.0 to Color(0xFFF9FCF2),
        55.0 to Color(0xFFFDF4E6),
        60.0 to Color(0xFFFAE5DB),
        65.0 to Color(0xFFF5DAD8),
        70.0 to Color(0xFFECD1DB),
        75.0 to Color(0xFFE3CEDE),
        80.0 to Color(0xFFD9CEDB),
    )


    // - Public functions

    /**
     * Get color palette mapping levels to a 0-1 scale based on given min and max values.
     *
     * @param palette Color palette to sample from
     * @param dbMin Target ramp lower bound in decibels
     * @param dbMax Target ramp upper bound in decibels
     * @param reversed If true, output ramp will be 0.0 for highest level, 1.0 for lowest level.
     */
    fun clamped(
        dbMin: Double = VuMeterOptions.DB_MIN,
        dbMax: Double = VuMeterOptions.DB_MAX,
        reversed: Boolean = false,
        palette: Map<Double, Color> = NoiseLevelColorRamp.palette,
    ): Map<Double, Color> {
        return palette.mapKeys { (spl, _) ->
            // Map spl index to a value between 0 and 1 based on min/max dB values
            val rampIndex = (spl - dbMin) / (dbMax - dbMin)

            if (reversed) {
                1.0 - rampIndex
            } else {
                rampIndex
            }
        }
    }

    /**
     * Get the color corresponding to the given SPL value
     *
     * @param palette Color palette to sample from
     * @param value SPL value
     *
     * @return Corresponding color from palette
     */
    fun getColorForSPLValue(
        value: Double,
        palette: Map<Double, Color> = NoiseLevelColorRamp.palette,
    ): Color {
        return palette.filter { it.key <= value }
            .minByOrNull { value - it.key }
            ?.value
            ?: Color.Black
    }
}
