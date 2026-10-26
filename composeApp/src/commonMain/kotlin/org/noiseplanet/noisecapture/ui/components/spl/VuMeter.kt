package org.noiseplanet.noisecapture.ui.components.spl

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.noiseplanet.noisecapture.ui.theme.ColorVariant
import org.noiseplanet.noisecapture.ui.theme.Noise
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.util.VuMeterOptions
import org.noiseplanet.noisecapture.util.ncDropShadow

private val BAR_HEIGHT: Dp = 24.dp

@Composable
fun VuMeter(
    valueFlow: StateFlow<Double>,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val ticks: List<Int> = remember {
        listOf(0, 20, 40, 60, 80, 100, 120)
    }

    val value: Double by valueFlow.collectAsStateWithLifecycle()
    val valueRatio = value / VuMeterOptions.DB_MAX

    val color by animateColorAsState(
        NoiseLevelColorRamp.getColorForSPLValue(
            value = value,
            variant = ColorVariant.DARK
        )
    )
    val shape = RoundedCornerShape(
        topStartPercent = 0,
        bottomStartPercent = 0,
        topEndPercent = 50,
        bottomEndPercent = 50
    )


    // - Layout

    Box(
        modifier = modifier.fillMaxWidth().height(24.dp),
    ) {
        // Ticks
        Row(
            modifier = Modifier.fillMaxSize()
                .background(Color.Noise.two.light)
                .border(width = 1.dp, color = Color.Noise.two.mediumLight)
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ticks.forEachIndexed { index, tick ->
                Text(
                    text = if (index == 0 || index == ticks.size - 1) "" else tick.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Noise.two.medium.copy(
                    ),
                    textAlign = TextAlign.End,
                )
            }
        }

        // Bar
        Box(
            modifier = Modifier
                .background(color, shape)
                .ncDropShadow(shape, color, alpha = 0.2f, blur = 25f)
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = 125,
                        easing = EaseOut
                    )
                )
                .fillMaxWidth(valueRatio.toFloat())
                .height(BAR_HEIGHT)
        )
    }
}


@Composable
@Preview(showBackground = true)
fun VuMeterPreview() {
    val valueFlow = MutableStateFlow(40.0)
    VuMeter(valueFlow = valueFlow)
}
