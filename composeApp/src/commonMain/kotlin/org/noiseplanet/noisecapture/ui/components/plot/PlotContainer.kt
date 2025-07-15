package org.noiseplanet.noisecapture.ui.components.plot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp


/**
 * Lays out X and Y axis with ticks and labels, then draws content in the remaining space.
 *
 * TODO: Add support for placing axes ticks in a non equal disposition (based on raw value)
 *
 * @param axisSettings X and Y axes settings.
 * @param modifier Modifier for the container.
 * @param content Plot content (lines, bars, etc...).
 */
@Composable
fun PlotContainer(
    axisSettings: PlotAxisSettings,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    // - Properties

    // TODO: Make this dynamic without triggering too much successive recompositions
    //       due to remeasuring content
    val xAxisTicksHeight = 20.dp
    val xAxisTicksCount = axisSettings.xTicks.size


    // - Layout

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        // Y axis if layout is left to right
        if (axisSettings.yAxisLayoutDirection == LayoutDirection.Ltr) {
            YAxisTicks(axisSettings, xAxisTicksHeight)
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier.weight(1f)
                    .clipToBounds()
            ) {
                content()
            }

            // X axis
            Column(
                modifier = Modifier.height(xAxisTicksHeight).fillMaxWidth()
            ) {
                // Ticks
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    axisSettings.xTicks.forEach { _ ->
                        VerticalDivider(
                            modifier = Modifier.height(4.dp),
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                }

                // Tick labels
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    axisSettings.xTicks.forEachIndexed { index, tick ->
                        AxisTickLabel(
                            text = tick.label,
                            textAlign = when (index) {
                                0 -> TextAlign.Start
                                xAxisTicksCount - 1 -> TextAlign.End
                                else -> TextAlign.Center
                            },
                            modifier = Modifier.weight(
                                when (index) {
                                    0, xAxisTicksCount - 1 -> 0.5f
                                    else -> 1.0f
                                }
                            ),
                        )
                    }
                }
            }
        }

        // Y axis if layout is right to left
        if (axisSettings.yAxisLayoutDirection == LayoutDirection.Rtl) {
            YAxisTicks(axisSettings, xAxisTicksHeight)
        }
    }
}


/**
 * Lays out Y axis ticks and labels
 */
@Composable
private fun YAxisTicks(
    axisSettings: PlotAxisSettings,
    xAxisTicksHeight: Dp,
) {
    // Y axis
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = when (axisSettings.yAxisLayoutDirection) {
            LayoutDirection.Ltr -> Alignment.End
            LayoutDirection.Rtl -> Alignment.Start
        },
        modifier = Modifier.fillMaxHeight()
            .padding(bottom = xAxisTicksHeight + 4.dp)
    ) {
        axisSettings.yTicks.reversed().forEach { tick ->
            AxisTickLabel(text = tick.label)
        }
    }
}


/**
 * A tick label with default styling.
 */
@Composable
private fun AxisTickLabel(
    text: String,
    textAlign: TextAlign = TextAlign.Unspecified,
    modifier: Modifier = Modifier,
) = Text(
    text = text,
    style = MaterialTheme.typography.labelSmall,
    fontWeight = FontWeight.SemiBold,
    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
    textAlign = textAlign,
    modifier = modifier
)
