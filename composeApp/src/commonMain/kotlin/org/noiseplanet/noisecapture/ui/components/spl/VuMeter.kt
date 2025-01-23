package org.noiseplanet.noisecapture.ui.components.spl

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterViewModel.Companion.VU_METER_DB_MAX
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterViewModel.Companion.VU_METER_DB_MIN
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp

private val BAR_HEIGHT: Dp = 24.dp

@Composable
fun VuMeter(
    ticks: IntArray,
    value: Double,
    minimum: Double = VU_METER_DB_MIN,
    maximum: Double = VU_METER_DB_MAX,
    modifier: Modifier = Modifier,
) {
    val valueRatio = (value - minimum) / (maximum - minimum)
    val color = NoiseLevelColorRamp.getColorForSPLValue(value)
    val shape = RoundedCornerShape(
        topStartPercent = 0,
        bottomStartPercent = 0,
        topEndPercent = 50,
        bottomEndPercent = 50
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Bar
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.fillMaxWidth(valueRatio.toFloat()).animateContentSize()
                    .height(BAR_HEIGHT)
                    .clip(shape)
                    .background(color)
            )
        }

        // Ticks
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ticks.forEach {
                Text(
                    text = it.toString(),
                    fontSize = 12.sp
                )
            }
        }
    }
}
