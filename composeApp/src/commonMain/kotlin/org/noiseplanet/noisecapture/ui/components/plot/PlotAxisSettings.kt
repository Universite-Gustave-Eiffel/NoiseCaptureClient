package org.noiseplanet.noisecapture.ui.components.plot

import androidx.compose.ui.unit.LayoutDirection


data class PlotAxisSettings(
    val xTicks: List<AxisTick> = emptyList(),
    val yTicks: List<AxisTick> = emptyList(),
    val yAxisLayoutDirection: LayoutDirection = LayoutDirection.Ltr,
)


data class AxisTick(
    val value: Double,
    val label: String,
)
