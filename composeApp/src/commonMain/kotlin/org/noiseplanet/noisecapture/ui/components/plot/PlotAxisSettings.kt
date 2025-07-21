package org.noiseplanet.noisecapture.ui.components.plot

import androidx.compose.ui.unit.LayoutDirection


/**
 * Plot axis settings.
 *
 * @param xTicks X axis ticks with value and label.
 * @param showXTickMarks If true, show tick mark on top of tick label.
 * @param yTicks Y axis ticks with value and label.
 * @param showYTickMarks If true, show tick mark next to tick label.
 * @param yAxisLayoutDirection If set to [LayoutDirection.Ltr], Y axis will be put on the left hand
 *                             side of the plot. Otherwise, it will be on the right hand side.
 */
data class PlotAxisSettings(
    val xTicks: List<AxisTick> = emptyList(),
    val showXTickMarks: Boolean = true,
    val yTicks: List<AxisTick> = emptyList(),
    val showYTickMarks: Boolean = true,
    val yAxisLayoutDirection: LayoutDirection = LayoutDirection.Ltr,
)


/**
 * An axis tick.
 *
 * @param value Tick value for positioning.
 * @param label Label for display.
 */
data class AxisTick(
    val value: Double,
    val label: String,
)
