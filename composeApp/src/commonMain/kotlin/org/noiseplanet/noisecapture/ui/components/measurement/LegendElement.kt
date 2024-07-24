package org.noiseplanet.noisecapture.ui.components.measurement

import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer

data class LegendElement(
    val text : TextLayoutResult,
    val xPos : Float,
    val textPos : Float,
    val depth : Int
)
