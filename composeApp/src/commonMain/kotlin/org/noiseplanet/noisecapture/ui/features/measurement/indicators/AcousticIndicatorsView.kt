package org.noiseplanet.noisecapture.ui.features.measurement.indicators

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import org.noiseplanet.noisecapture.ui.features.measurement.MAX_SHOWN_DBA_VALUE
import org.noiseplanet.noisecapture.ui.features.measurement.MIN_SHOWN_DBA_VALUE
import org.noiseplanet.noisecapture.ui.features.measurement.MeasurementStatistics
import org.noiseplanet.noisecapture.ui.features.measurement.NOISE_LEVEL_FONT_SIZE
import org.noiseplanet.noisecapture.ui.features.measurement.indicators.AcousticIndicatorsViewModel.Companion.VU_METER_DB_MAX
import org.noiseplanet.noisecapture.ui.features.measurement.indicators.AcousticIndicatorsViewModel.Companion.VU_METER_DB_MIN
import org.noiseplanet.noisecapture.ui.features.measurement.spectrum.SpectrumPlotViewModel.Companion.noiseColorRampSpl
import kotlin.math.round


@Composable
fun AcousticIndicatorsView(
    viewModel: AcousticIndicatorsViewModel,
) {
    val rightRoundedSquareShape: Shape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 40.dp,
        bottomStart = 0.dp,
        bottomEnd = 40.dp
    )
    val noiseLevel by viewModel.soundPressureLevelFlow.collectAsState(0.0)

    Column() {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                Modifier.padding(top = 20.dp, bottom = 10.dp).weight(1F),
                color = MaterialTheme.colorScheme.background,
                shape = rightRoundedSquareShape,
                shadowElevation = 10.dp
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    fontSize = TextUnit(
                                        18F,
                                        TextUnitType.Sp
                                    ),
                                )
                            )
                            { append("dB(A)") }
                        },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Text(
                        buildNoiseLevelText(noiseLevel),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
            Row(
                Modifier.align(Alignment.CenterVertically),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    MeasurementStatistics("Min", "-"),
                    MeasurementStatistics("Avg", "-"),
                    MeasurementStatistics("Max", "-")
                ).forEach {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Text(it.label)
                        Text(it.value)
                    }
                }
            }
        }

        VuMeter(
            ticks = viewModel.vuMeterTicks,
            minimum = VU_METER_DB_MIN,
            maximum = VU_METER_DB_MAX,
            value = noiseLevel,
            Modifier.fillMaxWidth()
                .height(50.dp)
                .padding(start = 30.dp, end = 30.dp),
        )
    }
}

@Composable
private fun buildNoiseLevelText(noiseLevel: Double): AnnotatedString = buildAnnotatedString {
    val inRangeNoise = noiseLevel > MIN_SHOWN_DBA_VALUE && noiseLevel < MAX_SHOWN_DBA_VALUE
    val colorIndex = noiseColorRampSpl.indexOfFirst { pair -> pair.first < noiseLevel }
    withStyle(
        style = SpanStyle(
            color = if (inRangeNoise) noiseColorRampSpl[colorIndex].second else MaterialTheme.colorScheme.onPrimary,
            fontSize = NOISE_LEVEL_FONT_SIZE,
            baselineShift = BaselineShift.None
        )
    ) {
        when {
            inRangeNoise -> append("${round(noiseLevel * 10) / 10}")
            else -> append("-")
        }
    }
}
