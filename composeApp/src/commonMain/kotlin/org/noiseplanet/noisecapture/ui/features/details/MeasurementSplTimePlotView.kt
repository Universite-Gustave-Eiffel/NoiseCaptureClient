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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
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
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.model.dao.LeqSequenceFragment
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import kotlin.time.Duration.Companion.milliseconds


@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun MeasurementSplTimePlotView(
    measurementId: String,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val viewModel: MeasurementSplTimePlotViewModel = koinInject { parametersOf(measurementId) }
    val leqSequence: List<LeqSequenceFragment> by viewModel.leqSequenceFlow.collectAsStateWithLifecycle()

    if (leqSequence.isEmpty()) return

    val plotData = leqSequence.first()
    val startTimestamp: Long =
        plotData.startTimestamp ?: 0 // leqSequence.mapNotNull { it.startTimestamp }.min()
    val endTimestamp: Long =
        plotData.endTimestamp ?: 0 // leqSequence.mapNotNull { it.endTimestamp }.max()


    // - Layout

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxWidth().height(400.dp)
    ) {
        Text(
            text = "Sound level over time",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = "Visualize the evolution of recorded global sound level over time.",
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
                    // TODO: Make axis label component
                    Text(
                        text = "${(it - startTimestamp).milliseconds.inWholeSeconds}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                },
                yAxisModel = DoubleLinearAxisModel(
                    range = 20.0..120.0,
                    minorTickCount = 1,
                ),
                yAxisLabels = @Composable {
                    Text(
                        text = it.toInt().toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                },
                horizontalMajorGridLineStyle = LineStyle(
                    brush = SolidColor(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.5f
                        )
                    ),
                    pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(10f, 10f))
                ),
                verticalMajorGridLineStyle = LineStyle(
                    brush = SolidColor(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.5f
                        )
                    ),
                    pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(10f, 10f))
                ),
                horizontalMinorGridLineStyle = LineStyle(
                    brush = SolidColor(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.3f
                        )
                    )
                ),
                verticalMinorGridLineStyle = LineStyle(
                    brush = SolidColor(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.2f
                        )
                    ),
                    blendMode = BlendMode.Multiply,
                )
            ) {
                val colorRamp = NoiseLevelColorRamp.ramp.reversed().map { (index, color) ->
                    Pair(1f - index, color)
                }
                val brush = Brush.verticalGradient(*colorRamp.toTypedArray())

                AreaPlot(
                    data = plotData.timestamp.mapIndexed { index, timestamp ->
                        Point(x = timestamp, y = plotData.lzeq[index])
                    },
                    lineStyle = LineStyle(
                        brush = brush,
                        strokeWidth = 2.dp
                    ),
                    areaBaseline = AreaBaseline.ConstantLine(value = 0.0),
                    areaStyle = AreaStyle(
                        brush = brush,
                        alpha = 0.3f,
                    )
                )
            }
        }
    }
}
