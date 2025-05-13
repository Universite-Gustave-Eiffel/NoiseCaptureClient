package org.noiseplanet.noisecapture.ui.components.spl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterViewModel.Companion.VU_METER_DB_MAX
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterViewModel.Companion.VU_METER_DB_MIN
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.util.roundTo


@Composable
fun SoundLevelMeterView(
    viewModel: SoundLevelMeterViewModel,
) {
    // - Properties

    val currentSoundPressureLevel by viewModel.soundPressureLevelFlow.collectAsState(0.0)
    val currentLeqMetrics by viewModel.laeqMetricsFlow.collectAsState(null)


    // - Layout

    Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
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
                        text = stringResource(viewModel.currentDbALabel),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                    )

                    val isSplInRange = currentSoundPressureLevel in VU_METER_DB_MIN..VU_METER_DB_MAX
                    val roundedSpl = currentSoundPressureLevel.roundTo(1)

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
                    LAeqMetricsView(metrics = currentLeqMetrics)
                }

                if (viewModel.showPlayPauseButton) {
                    NCButton(
                        viewModel = viewModel.playPauseButtonViewModel,
                        modifier = Modifier.size(40.dp)
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
}
