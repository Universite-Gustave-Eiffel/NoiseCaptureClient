package org.noiseplanet.noisecapture.ui.components.spl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterViewModel.Companion.VU_METER_DB_MAX
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterViewModel.Companion.VU_METER_DB_MIN
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import kotlin.math.round


@Composable
fun SoundLevelMeterView(
    viewModel: SoundLevelMeterViewModel,
) {
    // - Properties

    val currentSoundPressureLevel by viewModel.soundPressureLevelFlow.collectAsState(0.0)


    // - Layout

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = stringResource(viewModel.currentDbALabel),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
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
                        MeasurementStatistics(
                            label = stringResource(viewModel.minDbALabel),
                            value = "-",
                        ),
                        MeasurementStatistics(
                            label = stringResource(viewModel.avgDbALabel),
                            value = "-",
                        ),
                        MeasurementStatistics(
                            label = stringResource(viewModel.maxDbALabel),
                            value = "-",
                        ),
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
                NCButton(
                    onClick = viewModel::toggleAudioSource,
                    viewModel = viewModel.playPauseButtonViewModel
                )
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


private data class MeasurementStatistics(
    val label: String,
    val value: String,
)
