package org.noiseplanet.noisecapture.ui.features.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import io.github.koalaplot.core.ChartLayout
import io.github.koalaplot.core.line.AreaBaseline
import io.github.koalaplot.core.line.AreaPlot2
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
import org.noiseplanet.noisecapture.ui.components.plot.PlotAxisLabel
import org.noiseplanet.noisecapture.ui.components.plot.PlotGridLineStyle
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.util.VuMeterOptions
import org.noiseplanet.noisecapture.util.toHhMmSs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun SplTimePlotView(
    leqOverTime: Map<Long, Double>,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val startTimestamp: Long = leqOverTime.keys.min()
    val endTimestamp: Long = leqOverTime.keys.max()

    val colorRamp = NoiseLevelColorRamp.clamped(reversed = true)
        .map { (spl, color) -> Pair(spl.toFloat(), color) }
        .reversed()
    val gradientBrush = Brush.verticalGradient(*colorRamp.toTypedArray())


    // - Layout

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxWidth()
            .aspectRatio(1.25f)
            .heightIn(max = 200.dp)
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
                xAxisLabels = @Composable { timestamp ->
                    val sinceStart: Duration = (timestamp - startTimestamp).milliseconds
                    val hideHours = (endTimestamp - startTimestamp).milliseconds.inWholeHours == 0L

                    PlotAxisLabel(
                        text = sinceStart.toHhMmSs(hideHoursIfZero = hideHours),
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
                AreaPlot2(
                    data = leqOverTime.map { (timestamp, leq) ->
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
