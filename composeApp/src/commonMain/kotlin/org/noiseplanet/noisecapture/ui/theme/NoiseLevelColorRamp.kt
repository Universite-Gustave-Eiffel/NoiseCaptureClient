package org.noiseplanet.noisecapture.ui.theme

import androidx.compose.ui.graphics.Color
import org.noiseplanet.noisecapture.util.VuMeterOptions

/**
 * SPL color representation based on [this palette](https://www.coloringnoise.com/).
 */
object NoiseLevelColorRamp {

    // - Properties

    /**
     * Color palette with colors picked from [Coloring Noise](https://www.coloringnoise.com/)
     */
    val palette: Map<Double, ColorSet> = mapOf(
        0.0 to Color.Noise.one,
        30.0 to Color.Noise.one,
        35.0 to Color.Noise.two,
        40.0 to Color.Noise.three,
        45.0 to Color.Noise.four,
        50.0 to Color.Noise.five,
        55.0 to Color.Noise.six,
        60.0 to Color.Noise.seven,
        65.0 to Color.Noise.eight,
        70.0 to Color.Noise.nine,
        75.0 to Color.Noise.ten,
        80.0 to Color.Noise.eleven,
    )


    // - Public functions

    /**
     * Get color palette mapping levels to a 0-1 scale based on given min and max values.
     *
     * @param variant Target color palette variant
     * @param dbMin Target ramp lower bound in decibels
     * @param dbMax Target ramp upper bound in decibels
     * @param reversed If true, output ramp will be 0.0 for highest level, 1.0 for lowest level.
     */
    fun clamped(
        dbMin: Double = VuMeterOptions.DB_MIN,
        dbMax: Double = VuMeterOptions.DB_MAX,
        reversed: Boolean = false,
        variant: ColorVariant = ColorVariant.MEDIUM,
    ): Map<Double, Color> {
        return palette
            .mapValues { (_, value) ->
                value.getVariant(variant)
            }
            .mapKeys { (spl, _) ->
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
     * @param variant Target color palette variant
     * @param value SPL value
     *
     * @return Corresponding color from palette
     */
    fun getColorForSPLValue(
        value: Double,
        variant: ColorVariant = ColorVariant.MEDIUM,
    ): Color {
        return getColorSetForSPLValue(value)
            .getVariant(variant)
    }

    /**
     * Get the color set corresponding to the given SPL value
     * @param value SPL value
     *
     * @return Corresponding color set from palette
     */
    fun getColorSetForSPLValue(
        value: Double,
    ): ColorSet {
        return palette.filter { it.key <= value }
            .minByOrNull { value - it.key }
            ?.value
            ?: Color.Neutral
    }

    /**
     * Returns a [label: color] legend representation of the given palette
     *
     * @param variant Target color palette variant
     * @param descendingOrder If true, elements will be returned in descending order.
     * @return Legend label to color representation
     */
    fun paletteAsLegendElements(
        variant: ColorVariant = ColorVariant.MEDIUM,
        descendingOrder: Boolean = false,
    ): List<Pair<String, Color>> {
        val legendList = palette.filter { (level, _) -> level > 0 }.toList()

        val legend = legendList.mapIndexed { index, (level, color) ->
            val label = when (index) {
                0 -> "<${level.toInt()} dB(A)"
                legendList.lastIndex -> ">${level.toInt()} dB(A)"
                else -> "${level.toInt()}-${legendList[index + 1].first.toInt()} dB(A)"
            }
            Pair(label, color.getVariant(variant))
        }

        return if (descendingOrder) {
            legend.reversed()
        } else {
            legend
        }
    }
}
