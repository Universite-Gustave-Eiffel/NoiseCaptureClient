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
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.noiseplanet.noisecapture.ui.components.plot.PlotContainer
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
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
    val weightedSplBoxOffsets by derivedStateOf {
        splData.mapValues { (_, spl) ->
            ((max(spl.weighted, spl.raw) / xAxisMax)).toFloat()
        }.values.reversed()
    }

    // Gradient brush to paint the plot background
    val gradientBrush = remember {
        Brush.horizontalGradient(
            *NoiseLevelColorRamp.ramp.toTypedArray()
        )
    }
    val weightedSplBoxColor = MaterialTheme.colorScheme.onSurface


    // - Layout

    PlotContainer(
        axisSettings = axisSettings,
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.fillMaxSize()
        ) {
            rawSplBarWidths.forEachIndexed { index, widthFraction ->

                // 1. Pain the whole line using gradient brush
                Box(
                    modifier = Modifier.weight(1f)
                        .background(gradientBrush)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    // 2. Fill the right end of the bar with background color based on current value
                    Box(
                        modifier = Modifier.fillMaxHeight()
                            .background(backgroundColor)
                            .animateContentSize(
                                tween(
                                    easing = ANIMATION_CURVE,
                                    durationMillis = ANIMATION_DURATION_MS
                                )
                            )
                            .fillMaxWidth(fraction = 1f - widthFraction)
                    )

                    // 3. Add a grid on top of the bars to add vertical stripes
                    SpectrumPlotXAxisGrid(
                        xAxisTicks = (axisSettings.xTicks.size - 1) * 5,
                        lineColor = backgroundColor,
                    )

                    // 4. Add a grey box to represent weighted spl value.
                    val animatedBias by animateFloatAsState(
                        targetValue = weightedSplBoxOffsets[index],
                        animationSpec = tween(
                            easing = ANIMATION_CURVE,
                            durationMillis = ANIMATION_DURATION_MS
                        )
                    )

                    Box(
                        modifier = Modifier.fillMaxHeight()
                            .fillMaxWidth(fraction = 1f - animatedBias),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxHeight()
                                .width(WEIGHTED_SPL_BOX_WIDTH)
                                .background(weightedSplBoxColor)
                        )
                    }
                }

                // 5. For every bar except for the last, add a horizontal spacer
                if (index < rawSplBarWidths.size - 1) {
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
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
