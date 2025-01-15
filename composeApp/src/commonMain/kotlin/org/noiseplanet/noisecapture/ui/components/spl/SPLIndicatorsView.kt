package org.noiseplanet.noisecapture.ui.components.spl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.noiseplanet.noisecapture.ui.components.spl.SPLIndicatorsViewModel.Companion.VU_METER_DB_MAX
import org.noiseplanet.noisecapture.ui.components.spl.SPLIndicatorsViewModel.Companion.VU_METER_DB_MIN
import org.noiseplanet.noisecapture.ui.features.measurement.NOISE_LEVEL_FONT_SIZE
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import kotlin.math.round


@Composable
fun SPLIndicatorsView(
    viewModel: SPLIndicatorsViewModel,
) {
    val currentSoundPressureLevel by viewModel.soundPressureLevelFlow.collectAsState(0.0)
    val isAudioSourceRunning by viewModel.isAudioSourceRunningFlow.collectAsState(false)

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Current dB(A)", // TODO: Localize
                    style = MaterialTheme.typography.labelLarge,
                )

                val isSplInRange = currentSoundPressureLevel in VU_METER_DB_MIN..VU_METER_DB_MAX
                val roundedSpl = round(currentSoundPressureLevel * 10.0) / 10.0

                Text(
                    text = if (isSplInRange) roundedSpl.toString() else "-",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 36.sp,
                        color = NoiseLevelColorRamp.getColorForSPLValue(roundedSpl)
                    )
                )
            }

            if (viewModel.showMinMaxSPL) {
                Row(
                    Modifier.align(Alignment.Top),
                ) {
                    listOf(
                        // TODO: Localize this
                        MeasurementStatistics("Min", "-"),
                        MeasurementStatistics("Avg", "-"),
                        MeasurementStatistics("Max", "-")
                    ).forEach {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.width(50.dp),
                        ) {
                            Text(
                                text = it.label,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = it.value,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Start
                                )
                            )
                        }
                    }
                }
            }

            if (viewModel.showPlayPauseButton) {
                Button(onClick = {
                    if (isAudioSourceRunning) {
                        viewModel.stopListening()
                    } else {
                        viewModel.startListening()
                    }
                }) {
                    if (isAudioSourceRunning) {
                        Icon(imageVector = Icons.Filled.Pause, contentDescription = "Pause")
                    } else {
                        Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Play")
                    }
                }
            }
        }

        VuMeter(
            ticks = viewModel.vuMeterTicks,
            minimum = VU_METER_DB_MIN,
            maximum = VU_METER_DB_MAX,
            value = currentSoundPressureLevel,
        )
    }
}


@Composable
private fun buildNoiseLevelText(noiseLevel: Double): AnnotatedString = buildAnnotatedString {
    val inRangeNoise = noiseLevel > VU_METER_DB_MIN && noiseLevel < VU_METER_DB_MAX

    withStyle(
        style = SpanStyle(
            color = if (inRangeNoise) {
                NoiseLevelColorRamp.getColorForSPLValue(noiseLevel)
            } else {
                MaterialTheme.colorScheme.onPrimary
            },
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

private data class MeasurementStatistics(
    val label: String,
    val value: String,
)
