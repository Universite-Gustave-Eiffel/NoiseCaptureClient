package org.noiseplanet.noisecapture.ui.features.recording.plot.spectrum

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.noiseplanet.noisecapture.util.toFrequencyString
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
    val splData by viewModel.splDataFlow.collectAsStateWithLifecycle()

    // How wide is each frequency band bar relative to the width of the plot width
    val rawSplBarWidths by derivedStateOf {
        splData.mapValues { (_, spl) ->
            (spl.raw / axisSettings.maximumX).toFloat()
        }.values.reversed()
    }

    // Offset of each weighted spl value per frequency band (-1 is left aligned, 1 is right aligned)
    val weightedSplBoxBiases by derivedStateOf {
        splData.mapValues { (_, spl) ->
            ((max(spl.weighted, spl.raw) / axisSettings.maximumX) * 2.0 - 1.0).toFloat()
        }.values.reversed()
    }

    // Gradient brush to paint the plot background
    val gradientBrush = remember {
        Brush.horizontalGradient(
            *viewModel.spectrumColorRamp.toTypedArray()
        )
    }


    // - Layout

    SpectrumPlotContainer(axisSettings, modifier) {
        // 1. Paint the entire background using the gradient brush
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.fillMaxSize()
                .background(gradientBrush)
        ) {
            // 2. Adjust bars size by clipping from the end of the screen (i.e. drawing the negative)
            for (widthFraction in rawSplBarWidths) {
                Box(
                    modifier = Modifier.weight(1f)
                        .background(backgroundColor)
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
            xAxisTicks = axisSettings.xTicksCount * 5,
            lineColor = backgroundColor,
        )

        // 4. Draw dark rectangles to show weighted spl values
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            val boxColor = MaterialTheme.colorScheme.onSurface

            weightedSplBoxBiases.forEach { bias ->
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
                        .background(boxColor)
                        .align(BiasAlignment.Horizontal(animatedBias))
                )
            }
        }

        // 5. Add horizontal grid on top of everything to separate bars
        SpectrumPlotYAxisGrid(
            yAxisTicks = axisSettings.nominalFrequencies.size,
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
) = Canvas(modifier = Modifier.fillMaxSize()) {
    val strokeWidthPx = strokeWidth.toPx()
    val xOffsetStep = size.height / xAxisTicks
    var xOffset = xOffsetStep - strokeWidthPx / 2f

    repeat(xAxisTicks - 1) {
        drawLine(
            color = lineColor,
            start = Offset(xOffset, 0f),
            end = Offset(xOffset, size.height),
            strokeWidth = strokeWidthPx
        )
        xOffset += xOffsetStep
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
) = Canvas(modifier = Modifier.fillMaxSize()) {
    val strokeWidthPx = strokeWidth.toPx()
    val yOffsetStep = (size.height + strokeWidthPx) / yAxisTicks
    var yOffset = yOffsetStep - strokeWidthPx / 2f

    repeat(yAxisTicks - 1) {
        drawLine(
            color = lineColor,
            start = Offset(0f, yOffset),
            end = Offset(size.width, yOffset),
            strokeWidth = strokeWidthPx
        )
        yOffset += yOffsetStep
    }
}


/**
 * Lays out X and Y axes with given content.
 */
@Composable
private fun SpectrumPlotContainer(
    axisSettings: SpectrumPlotViewModel.AxisSettings,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    // - Properties

    val xAxisTicksHeight = 12.dp


    // - Layout

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        // Y axis
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End,
            modifier = Modifier.fillMaxHeight()
                .padding(bottom = xAxisTicksHeight + 4.dp)
        ) {
            for (freq in axisSettings.nominalFrequencies.reversed()) {
                AxisTickLabel(text = freq.toFrequencyString())
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                content()
            }

            // X axis
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(xAxisTicksHeight).fillMaxWidth()
            ) {
                val tickStep = (axisSettings.maximumX / axisSettings.xTicksCount).toInt()
                val tickRange = axisSettings.minimumX.toInt()..axisSettings.maximumX.toInt()
                for (tick in tickRange step tickStep) {
                    AxisTickLabel(text = "$tick dB")
                }
            }
        }
    }
}


@Composable
private fun AxisTickLabel(text: String) = Text(
    text = text,
    style = MaterialTheme.typography.labelSmall,
    fontWeight = FontWeight.SemiBold,
    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
)
