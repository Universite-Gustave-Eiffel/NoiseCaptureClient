package org.noiseplanet.noisecapture.ui.features.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.koalaplot.core.ChartLayout
import io.github.koalaplot.core.line.AreaBaseline
import io.github.koalaplot.core.line.AreaPlot
import io.github.koalaplot.core.style.AreaStyle
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.DoubleLinearAxisModel
import io.github.koalaplot.core.xygraph.LongLinearAxisModel
import io.github.koalaplot.core.xygraph.Point
import io.github.koalaplot.core.xygraph.XYGraph
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_details_spl_time_plot_description
import noisecapture.composeapp.generated.resources.measurement_details_spl_time_plot_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.ui.components.plot.PlotAxisLabel
import org.noiseplanet.noisecapture.ui.components.plot.PlotGridLineStyle
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.util.VuMeterOptions
import kotlin.time.Duration.Companion.milliseconds


@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun MeasurementSplTimePlotView(
    measurementId: String,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val viewModel: MeasurementSplTimePlotViewModel = koinInject { parametersOf(measurementId) }
    val plotData: Map<Long, Double> by viewModel.plotDataFlow.collectAsStateWithLifecycle()

    if (plotData.isEmpty()) return

    val startTimestamp: Long = plotData.keys.min()
    val endTimestamp: Long = plotData.keys.max()

    val colorRamp = NoiseLevelColorRamp.clamped(reversed = true)
        .map { (spl, color) -> Pair(spl.toFloat(), color) }
        .reversed()
    val gradientBrush = Brush.verticalGradient(*colorRamp.toTypedArray())


    // - Layout

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxWidth().height(400.dp)
    ) {
        Text(
            text = stringResource(Res.string.measurement_details_spl_time_plot_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = stringResource(Res.string.measurement_details_spl_time_plot_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )

        ChartLayout(
            modifier = Modifier.padding(top = 16.dp)
        ) {
            XYGraph(
                xAxisModel = LongLinearAxisModel(
                    range = startTimestamp..endTimestamp,
                    minorTickCount = 1,
                ),
                xAxisLabels = @Composable {
                    PlotAxisLabel(
                        text = "${(it - startTimestamp).milliseconds.inWholeSeconds}s",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                },
                yAxisModel = DoubleLinearAxisModel(
                    range = VuMeterOptions.DB_MIN..VuMeterOptions.DB_MAX,
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

                AreaPlot(
                    data = plotData.map { (timestamp, leq) ->
                        Point(x = timestamp, y = leq)
                    },
                    lineStyle = LineStyle(
                        brush = gradientBrush,
                        strokeWidth = 2.dp
                    ),
                    areaBaseline = AreaBaseline.ConstantLine(value = 0.0),
                    areaStyle = AreaStyle(
                        brush = gradientBrush,
                        alpha = 0.5f,
                    )
                )
            }
        }
    }
}
