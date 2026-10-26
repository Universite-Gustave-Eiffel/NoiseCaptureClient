package org.noiseplanet.noisecapture.ui.theme

import androidx.compose.ui.graphics.Color

// -----------------------------------------------------------------------
// Noise levels palette with lighter and darker tones
// Base colors taken from Coloring noise palette by Beate Tomio:
// https://www.coloringnoise.com/theoretical_background/new-color-scheme/
// -----------------------------------------------------------------------

interface NoiseColorPalette {

    val one: ColorSet
    val two: ColorSet
    val three: ColorSet
    val four: ColorSet
    val five: ColorSet
    val six: ColorSet
    val seven: ColorSet
    val eight: ColorSet
    val nine: ColorSet
    val ten: ColorSet
    val eleven: ColorSet
}

val Color.Companion.Noise
    get() = object : NoiseColorPalette {

        override val one = ColorSet(
            light = Color(0xFFE6EDEF),
            mediumLight = Color(0xFFC0D2D6),
            medium = Color(0xFF82A6AD),
            dark = Color(0xFF576F73),
        )

        override val two = ColorSet(
            light = Color(0xFFECF1F2),
            mediumLight = Color(0xFFCFDCDF),
            medium = Color(0xFFA0BABF),
            dark = Color(0xFF6B7C7F),
        )

        override val three = ColorSet(
            light = Color(0xFFF1F7F6),
            mediumLight = Color(0xFFDBEAE8),
            medium = Color(0xFFB8D6D1),
            dark = Color(0xFF7B8F8B),
        )

        override val four = ColorSet(
            light = Color(0xFFF5FAF5),
            mediumLight = Color(0xFFE6F1E5),
            medium = Color(0xFFCEE4CC),
            dark = Color(0xFF899888),
        )

        override val five = ColorSet(
            light = Color(0xFFF9FCF2),
            mediumLight = Color(0xFFF0F8DF),
            medium = Color(0xFFE2F2BF),
            dark = Color(0xFF97A17F),
        )

        override val six = ColorSet(
            light = Color(0xFFFDF4E6),
            mediumLight = Color(0xFFF9E2C1),
            medium = Color(0xFFF3C683),
            dark = Color(0xFFA28457),
        )

        override val seven = ColorSet(
            light = Color(0xFFFAE5DB),
            mediumLight = Color(0xFFF3BEA6),
            medium = Color(0xFFE87E4D),
            dark = Color(0xFFC16940),
        )

        override val eight = ColorSet(
            light = Color(0xFFF5DAD8),
            mediumLight = Color(0xFFE6A29E),
            medium = Color(0xFFCD463E),
            dark = Color(0xFFCD463E),
        )

        override val nine = ColorSet(
            light = Color(0xFFECD1DB),
            mediumLight = Color(0xFFD08CA6),
            medium = Color(0xFFA11A4D),
            dark = Color(0xFFA11A4D),
        )

        override val ten = ColorSet(
            light = Color(0xFFE3CEDE),
            mediumLight = Color(0xFFBA83AD),
            medium = Color(0xFF75085C),
            dark = Color(0xFF75085C),
        )

        override val eleven = ColorSet(
            light = Color(0xFFD9CEDB),
            mediumLight = Color(0xFFA184A4),
            medium = Color(0xFF430A4A),
            dark = Color(0xFF430A4A),
        )
    }

// -----------------------------------------------------------------------
// App specific colors
// -----------------------------------------------------------------------

val Color.Companion.Surface get() = Color(0xFFFFFFFF)
val Color.Companion.SurfaceContainer get() = Color(0xFFFAF9F9)
val Color.Companion.InverseSurface get() = Color(0xFF0D0E0F)
val Color.Companion.OnSurface get() = Color(0xFF313333)
val Color.Companion.OnSurfaceVariant get() = Color(0xFF6A6969)

val Color.Companion.Neutral
    get() = ColorSet(
        light = Color.SurfaceContainer,
        mediumLight = Color.Noise.one.mediumLight,
        medium = Color.OnSurfaceVariant,
        dark = Color.OnSurface
    )

val Color.Companion.AccentBlue get() = Color(0xFF4E7EE5)


// -----------------------------------------------------------------------
// Types
// -----------------------------------------------------------------------

data class ColorSet(
    val light: Color,
    val mediumLight: Color,
    val medium: Color,
    val dark: Color,
) {

    fun getVariant(variant: ColorVariant): Color {
        return when (variant) {
            ColorVariant.LIGHT -> light
            ColorVariant.MEDIUM_LIGHT -> mediumLight
            ColorVariant.MEDIUM -> medium
            ColorVariant.DARK -> dark
        }
    }
}

enum class ColorVariant {
    LIGHT,
    MEDIUM_LIGHT,
    MEDIUM,
    DARK
}
