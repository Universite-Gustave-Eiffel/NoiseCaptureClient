package org.noiseplanet.noisecapture.ui.features.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import io.github.koalaplot.core.ChartLayout
import io.github.koalaplot.core.bar.DefaultBar
import io.github.koalaplot.core.bar.DefaultBarPosition
import io.github.koalaplot.core.bar.DefaultVerticalBarPlotEntry
import io.github.koalaplot.core.bar.VerticalBarPlot
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.DoubleLinearAxisModel
import io.github.koalaplot.core.xygraph.XYGraph
import io.github.koalaplot.core.xygraph.rememberAxisStyle
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_details_avg_spl_per_freq_plot_description
import noisecapture.composeapp.generated.resources.measurement_details_avg_spl_per_freq_plot_title
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.components.plot.PlotAxisLabel
import org.noiseplanet.noisecapture.ui.components.plot.PlotGridLineStyle
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.util.toFrequencyString


@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun AverageLevelPerFreqView(
    avgLevelPerFreq: Map<Int, Double>,
    modifier: Modifier = Modifier,
) {
    // - Layout

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxWidth()
            .aspectRatio(1.25f)
            .heightIn(max = 200.dp)
    ) {
        Text(
            text = stringResource(Res.string.measurement_details_avg_spl_per_freq_plot_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = stringResource(Res.string.measurement_details_avg_spl_per_freq_plot_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )

        ChartLayout(
            modifier = Modifier.padding(top = 16.dp)
        ) {
            XYGraph(
                xAxisModel = CategoryAxisModel(
                    categories = avgLevelPerFreq.keys.toList(),
                ),
                xAxisLabels = @Composable { x ->
                    if (avgLevelPerFreq.keys.toList().indexOf(x) % 5 == 0) {
                        PlotAxisLabel(
                            text = x.toFrequencyString(),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(end = 4.dp, top = 4.dp)
                        )
                    }
                },
                xAxisStyle = rememberAxisStyle(labelRotation = 45),
                yAxisModel = DoubleLinearAxisModel(
                    range = 0.0..100.0,
                    minorTickCount = 1,
                ),
                yAxisLabels = @Composable {
                    PlotAxisLabel(
                        text = it.toInt().toString(),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                },
                horizontalMajorGridLineStyle = PlotGridLineStyle.majorHorizontal,
                verticalMajorGridLineStyle = PlotGridLineStyle.majorVertical,
                horizontalMinorGridLineStyle = PlotGridLineStyle.minorHorizontal,
                verticalMinorGridLineStyle = PlotGridLineStyle.minorVertical,
            ) {

                VerticalBarPlot(
                    data = avgLevelPerFreq.toList().map { (freq, level) ->
                        DefaultVerticalBarPlotEntry(
                            x = freq,
                            y = DefaultBarPosition(0.0, level)
                        )
                    },
                    bar = { index, _, entry ->
                        DefaultBar(
                            brush = SolidColor(NoiseLevelColorRamp.getColorForSPLValue(entry.y.end)),
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                )
            }
        }
    }
}
