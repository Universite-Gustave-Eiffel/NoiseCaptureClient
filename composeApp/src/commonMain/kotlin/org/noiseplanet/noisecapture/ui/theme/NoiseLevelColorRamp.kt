package org.noiseplanet.noisecapture.ui.theme

import androidx.compose.ui.graphics.Color
import org.noiseplanet.noisecapture.util.VuMeterOptions

/**
 * SPL color representation based on [this palette](https://www.coloringnoise.com/).
 */
object NoiseLevelColorRamp {

    // - Properties

    val level1 = Color(0xFF82A6AD)
    val level1Light = Color(0xFFE6EDEF)
    val level1Dark = Color(0xFF576F73)

    val level2 = Color(0xFFA0BABF)
    val level2Light = Color(0xFFECF1F2)
    val level2Dark = Color(0xFF6B7C7F)

    val level3 = Color(0xFFB8D6D1)
    val level3Light = Color(0xFFF1F7F6)
    val level3Dark = Color(0xFF7B8F8B)

    val level4 = Color(0xFFCEE4CC)
    val level4Light = Color(0xFFF5FAF5)
    val level4Dark = Color(0xFF899888)

    val level5 = Color(0xFFE2F2BF)
    val level5Light = Color(0xFFF9FCF2)
    val level5Dark = Color(0xFF97A17F)

    val level6 = Color(0xFFF3C683)
    val level6Light = Color(0xFFFDF4E6)
    val level6Dark = Color(0xFFA28457)

    val level7 = Color(0xFFE87E4D)
    val level7Light = Color(0xFFFAE5DB)
    val level7Dark = Color(0xFFC16940)

    val level8 = Color(0xFFCD463E)
    val level8Light = Color(0xFFF5DAD8)
    val level8Dark = Color(0xFFCD463E)

    val level9 = Color(0xFFA11A4D)
    val level9Light = Color(0xFFECD1DB)
    val level9Dark = Color(0xFFA11A4D)

    val level10 = Color(0xFF75085C)
    val level10Light = Color(0xFFE3CEDE)
    val level10Dark = Color(0xFF75085C)

    val level11 = Color(0xFF430A4A)
    val level11Light = Color(0xFFD9CEDB)
    val level11Dark = Color(0xFF430A4A)


    /**
     * Base color palette with true colors picked from [Coloring Noise](https://www.coloringnoise.com/)
     */
    val palette: Map<Double, Color> = mapOf(
        0.0 to level1,
        30.0 to level1,
        35.0 to level2,
        40.0 to level3,
        45.0 to level4,
        50.0 to level5,
        55.0 to level6,
        60.0 to level7,
        65.0 to level8,
        70.0 to level9,
        75.0 to level10,
        80.0 to level11,
    )

    /**
     * A darker version of the color palette to provide better contrast against light backgrounds
     */
    val paletteDarker: Map<Double, Color> = mapOf(
        0.0 to level1Dark,
        30.0 to level1Dark,
        35.0 to level2Dark,
        40.0 to level3Dark,
        45.0 to level4Dark,
        50.0 to level5Dark,
        55.0 to level6Dark,
        60.0 to level7Dark,
        65.0 to level8Dark,
        70.0 to level9Dark,
        75.0 to level10Dark,
        80.0 to level11Dark,
    )

    /**
     * A lighter version of the color palette to use as background tint.
     */
    val paletteLighter: Map<Double, Color> = mapOf(
        0.0 to level1Light,
        30.0 to level1Light,
        35.0 to level2Light,
        40.0 to level3Light,
        45.0 to level4Light,
        50.0 to level5Light,
        55.0 to level6Light,
        60.0 to level7Light,
        65.0 to level8Light,
        70.0 to level9Light,
        75.0 to level10Light,
        80.0 to level11Light,
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

    /**
     * Returns a [label: color] legend representation of the given palette
     *
     * @param palette Target color palette
     * @param descendingOrder If true, elements will be returned in descending order.
     * @return Legend label to color representation
     */
    fun paletteAsLegendElements(
        palette: Map<Double, Color> = NoiseLevelColorRamp.palette,
        descendingOrder: Boolean = false,
    ): List<Pair<String, Color>> {
        val legendList = palette.filter { (level, _) -> level > 0 }.toList()

        val legend = legendList.mapIndexed { index, (level, color) ->
            val label = when (index) {
                0 -> "<${level.toInt()} dB(A)"
                legendList.lastIndex -> ">${level.toInt()} dB(A)"
                else -> "${level.toInt()}-${legendList[index + 1].first.toInt()} dB(A)"
            }
            Pair(label, color)
        }

        return if (descendingOrder) {
            legend.reversed()
        } else {
            legend
        }
    }
}
