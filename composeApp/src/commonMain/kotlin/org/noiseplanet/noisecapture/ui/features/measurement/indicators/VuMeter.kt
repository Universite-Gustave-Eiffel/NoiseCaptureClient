package org.noiseplanet.noisecapture.ui.features.measurement.indicators

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import org.noiseplanet.noisecapture.ui.features.measurement.plot.spectrum.SpectrumPlotViewModel.Companion.noiseColorRampSpl
import kotlin.math.max
import kotlin.math.min

@Composable
fun VuMeter(
    ticks: IntArray,
    minimum: Double,
    maximum: Double,
    value: Double,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme
    val textMeasurer = rememberTextMeasurer()

    // TODO: Rewrite this using Compose

    Canvas(modifier = modifier) {
        // x axis labels
        var maxHeight = 0
        ticks.forEach { value ->
            val textLayoutResult = textMeasurer.measure(buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontSize = TextUnit(
                            10F,
                            TextUnitType.Sp
                        )
                    )
                )
                { append("$value") }
            })
            maxHeight = max(textLayoutResult.size.height, maxHeight)
            val labelRatio =
                max(0.0, (value - minimum) / (maximum - minimum))
            val xPosition = min(
                size.width - textLayoutResult.size.width,
                max(
                    0F,
                    (size.width * labelRatio - textLayoutResult.size.width / 2).toFloat()
                )
            )
            drawText(textLayoutResult, topLeft = Offset(xPosition, 0F))
        }
        val barHeight = size.height - maxHeight
        drawRoundRect(
            color = color.background,
            topLeft = Offset(0F, maxHeight.toFloat()),
            cornerRadius = CornerRadius(barHeight / 2, barHeight / 2),
            size = Size(size.width, barHeight)
        )
        val valueRatio = (value - minimum) / (maximum - minimum)
        val colorIndex = noiseColorRampSpl.indexOfFirst { pair -> pair.first < value }
        drawRoundRect(
            color = noiseColorRampSpl[colorIndex].second,
            topLeft = Offset(0F, maxHeight.toFloat()),
            cornerRadius = CornerRadius(barHeight / 2, barHeight / 2),
            size = Size((size.width * valueRatio).toFloat(), barHeight)
        )
    }
}
