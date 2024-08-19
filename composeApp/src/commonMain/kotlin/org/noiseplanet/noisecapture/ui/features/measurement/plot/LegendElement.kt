package org.noiseplanet.noisecapture.ui.features.measurement.plot

import androidx.compose.ui.text.TextLayoutResult

data class LegendElement(
    val text: TextLayoutResult,
    val xPos: Float,
    val textPos: Float,
    val depth: Int,
)
