package org.noiseplanet.noisecapture.ui.features.measurement.plot

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap


data class PlotBitmapOverlay(
    val imageBitmap: ImageBitmap,
    val imageSize: Size,
    val horizontalLegendSize: Size,
    val verticalLegendSize: Size,
    val plotSettingsHashCode: Int,
)
