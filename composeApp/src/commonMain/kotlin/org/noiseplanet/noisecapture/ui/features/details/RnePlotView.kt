package org.noiseplanet.noisecapture.ui.features.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.koalaplot.core.pie.DefaultSlice
import io.github.koalaplot.core.pie.PieChart
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.details_rne_plot_description
import noisecapture.composeapp.generated.resources.details_rne_plot_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.util.roundTo


/**
 * A pie chart of Repartition of Noise Exposure per level threshold.
 */
@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun RnePlotView(
    rneData: Map<Double, Double>,
    modifier: Modifier = Modifier,
) {
    // - Properties

    var selectedSliceIndex: Int? by remember { mutableStateOf(null) }
    val legendData = remember {
        NoiseLevelColorRamp.palette.keys.mapIndexed { index, level ->
            when (index) {
                0 -> "<${rneData.keys.elementAt(index + 1).toInt()} dB(A)"
                rneData.keys.size - 1 -> ">${level.toInt()} dB(A)"
                else -> "${level.toInt()}-${rneData.keys.elementAt(index + 1).toInt()} dB(A)"
            }
        }.zip(NoiseLevelColorRamp.palette.values).toMap()
    }


    // - Subviews

    @Composable
    fun LegendElement(
        color: Color,
        label: String,
        index: Int,
        modifier: Modifier = Modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.wrapContentWidth()
                .clickable(onClick = {
                    selectedSliceIndex = index
                })
                .padding(vertical = 4.dp),
        ) {
            Box(
                modifier = Modifier.size(12.dp)
                    .background(color = color, shape = CircleShape)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }


    // - Layout

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(Res.string.details_rne_plot_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = stringResource(Res.string.details_rne_plot_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )

        Row(
            modifier = Modifier.padding(top = 16.dp)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier.weight(1f)
                    .aspectRatio(1f)
                    .height(IntrinsicSize.Min) // Match the row height
            ) {
                PieChart(
                    modifier = Modifier.fillMaxHeight().aspectRatio(1f),
                    values = rneData.values.map { it.toFloat() },
                    holeSize = 0.4f,
                    label = {},
                    labelConnector = {},
                    labelSpacing = 1f,
                    maxPieDiameter = 200.dp,
                    slice = @Composable { sliceIndex ->
                        val colors = remember { NoiseLevelColorRamp.palette.values.toList() }
                        val borderColors = remember {
                            NoiseLevelColorRamp.paletteLighter.values.toList()
                        }

                        DefaultSlice(
                            colors[sliceIndex],
                            onClick = {
                                selectedSliceIndex = if (selectedSliceIndex != sliceIndex) {
                                    sliceIndex
                                } else {
                                    null
                                }
                            },
                            clickable = true,
                            hoverExpandFactor = 1.05f,
                            antiAlias = true,
                            border = if (selectedSliceIndex == sliceIndex) {
                                BorderStroke(width = 4.dp, color = borderColors[sliceIndex])
                            } else {
                                null
                            }
                        )
                    },
                    holeContent = {
                        val index = selectedSliceIndex ?: return@PieChart run {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "RNE",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                )
                            }
                        }
                        val percentage = rneData.values.elementAt(index) * 100.0
                        val foreground = NoiseLevelColorRamp.paletteDarker.values.elementAt(index)

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${percentage.roundTo(1)}%",
                                color = foreground,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            )
                        }
                    }
                )
            }

            Row(
                modifier = Modifier.wrapContentWidth()
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                legendData.keys.chunked(6).forEach { chunk ->
                    Column {
                        chunk.forEach { label ->
                            legendData[label]?.let { color ->
                                val index = legendData.keys.indexOf(label)
                                LegendElement(color, label, index)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun RnePlotViewPreview() {
    RnePlotView(
        rneData = NoiseLevelColorRamp.palette.keys
            .zip(MutableList(NoiseLevelColorRamp.palette.keys.size) { 100 / 11.0 })
            .toMap()
    )
}
