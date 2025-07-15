package org.noiseplanet.noisecapture.ui.features.recording.plot.spectrum

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.noiseplanet.noisecapture.ui.components.plot.PlotContainer
import kotlin.math.max

private const val ANIMATION_DURATION_MS = 400
private val ANIMATION_CURVE = EaseOutBack
private val WEIGHTED_SPL_BOX_WIDTH = 10.dp


@Composable
fun SpectrumPlotView(
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val viewModel: SpectrumPlotViewModel = koinViewModel()

    val axisSettings by viewModel.axisSettingsFlow.collectAsStateWithLifecycle()
    val xAxisMax: Double = axisSettings.xTicks.maxOfOrNull { it.value } ?: 1.0

    val splData by viewModel.splDataFlow.collectAsStateWithLifecycle()

    // How wide is each frequency band bar relative to the width of the plot width
    val rawSplBarWidths by derivedStateOf {
        splData.mapValues { (_, spl) ->
            (spl.raw / xAxisMax).toFloat()
        }.values.reversed()
    }

    // Offset of each weighted spl value per frequency band (-1 is left aligned, 1 is right aligned)
    val weightedSplBoxBiases by derivedStateOf {
        splData.mapValues { (_, spl) ->
            ((max(spl.weighted, spl.raw) / xAxisMax) * 2.0 - 1.0).toFloat()
        }.values.reversed()
    }

    // Gradient brush to paint the plot background
    val gradientBrush = remember {
        Brush.horizontalGradient(
            *viewModel.spectrumColorRamp.toTypedArray()
        )
    }


    // - Layout

    PlotContainer(
        axisSettings = axisSettings,
        modifier = modifier,
    ) {
        // 1. Paint the entire background using the gradient brush
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.fillMaxSize()
                .background(gradientBrush)
        ) {
            // 2. Adjust bars size by clipping from the end of the screen (i.e. drawing the negative)
            rawSplBarWidths.forEach { widthFraction ->
                Box(
                    modifier = Modifier.weight(1f)
                        .background(Color.Red)
                        .animateContentSize(
                            tween(
                                easing = ANIMATION_CURVE,
                                durationMillis = ANIMATION_DURATION_MS
                            )
                        )
                        .fillMaxWidth(fraction = 1f - widthFraction)
                )
            }
        }

        // 3. Add a grid on top of the bars to add vertical stripes
        SpectrumPlotXAxisGrid(
            xAxisTicks = (axisSettings.xTicks.size - 1) * 5,
            lineColor = backgroundColor,
        )

        // 4. Draw dark rectangles to show weighted spl values
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            val boxColor = MaterialTheme.colorScheme.onSurface

            weightedSplBoxBiases.forEachIndexed { index, bias ->
                val animatedBias by animateFloatAsState(
                    targetValue = bias,
                    animationSpec = tween(
                        easing = ANIMATION_CURVE,
                        durationMillis = ANIMATION_DURATION_MS
                    )
                )

                Box(
                    modifier = Modifier.weight(1f)
                        .width(WEIGHTED_SPL_BOX_WIDTH)
                        //.padding(bottom = if (index == weightedSplBoxBiases.size - 1) 0.dp else 2.dp)
                        .background(boxColor)
                        .align(BiasAlignment.Horizontal(animatedBias))
                )
            }
        }

        // 5. Add horizontal grid on top of everything to separate bars
        SpectrumPlotYAxisGrid(
            yAxisTicks = axisSettings.yTicks.size,
            lineColor = backgroundColor
        )
    }
}


/**
 * Lays out a grid of given X vertical lines with given line color.
 */
@Composable
private fun SpectrumPlotXAxisGrid(
    xAxisTicks: Int,
    lineColor: Color,
    strokeWidth: Dp = 2.dp,
) = Row(
    horizontalArrangement = Arrangement.SpaceEvenly,
    modifier = Modifier.fillMaxSize()
) {
    repeat(xAxisTicks - 1) {
        VerticalDivider(
            thickness = strokeWidth,
            modifier = Modifier.fillMaxHeight(),
            color = lineColor
        )
    }
}


/**
 * Lays out a grid of given Y horizontal lines with given line color.
 */
@Composable
private fun SpectrumPlotYAxisGrid(
    yAxisTicks: Int,
    lineColor: Color,
    strokeWidth: Dp = 2.dp,
) = Column(
    verticalArrangement = Arrangement.SpaceEvenly,
    modifier = Modifier.fillMaxSize()
) {
    repeat(yAxisTicks - 1) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(lineColor)
                .height(strokeWidth),
        )
    }
}
