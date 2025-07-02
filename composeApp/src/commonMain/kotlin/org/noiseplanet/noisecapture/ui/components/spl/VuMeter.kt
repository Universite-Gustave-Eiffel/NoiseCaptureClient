package org.noiseplanet.noisecapture.ui.components.spl

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.util.VuMeterOptions

private val BAR_HEIGHT: Dp = 24.dp

@Composable
fun VuMeter(
    ticks: IntArray,
    valueFlow: StateFlow<Double>,
    minimum: Double = VuMeterOptions.DB_MIN,
    maximum: Double = VuMeterOptions.DB_MAX,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val value: Double by valueFlow.collectAsStateWithLifecycle()
    val valueRatio = (value - minimum) / (maximum - minimum)

    val color = NoiseLevelColorRamp.getColorForSPLValue(value)
    val shape = RoundedCornerShape(
        topStartPercent = 0,
        bottomStartPercent = 0,
        topEndPercent = 50,
        bottomEndPercent = 50
    )


    // - Layout

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Bar
        Box(
            modifier = Modifier
                .clip(shape)
                .background(color)
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = 125,
                        easing = EaseOut
                    )
                )
                .fillMaxWidth(valueRatio.toFloat())
                .height(BAR_HEIGHT)
        )

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
