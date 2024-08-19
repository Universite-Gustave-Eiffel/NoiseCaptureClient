package org.noiseplanet.noisecapture.ui.features.measurement.indicators

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import org.noiseplanet.noisecapture.ui.features.measurement.plot.spectrum.SpectrumPlotViewModel.Companion.noiseColorRampSpl

private val BAR_HEIGHT: Dp = 32.dp

@Composable
fun VuMeter(
    ticks: IntArray,
    minimum: Double,
    maximum: Double,
    value: Double,
    modifier: Modifier = Modifier,
) {
    val valueRatio = (value - minimum) / (maximum - minimum)
    val colorIndex = noiseColorRampSpl.indexOfFirst { pair -> pair.first < value }
    val color = noiseColorRampSpl[colorIndex].second

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(valueRatio.toFloat())
                    .height(BAR_HEIGHT)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(color)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ticks.forEach {
                Text(text = "$it", fontSize = TextUnit(10F, TextUnitType.Sp))
            }
        }
    }
}
