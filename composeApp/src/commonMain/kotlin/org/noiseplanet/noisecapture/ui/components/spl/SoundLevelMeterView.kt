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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.util.isInVuMeterRange
import org.noiseplanet.noisecapture.util.roundTo


@Composable
fun SoundLevelMeterView(
    viewModel: SoundLevelMeterViewModel,
) {
    // - Properties

    val currentSoundPressureLevel by viewModel.soundPressureLevelFlow.collectAsStateWithLifecycle()
    val roundedSpl = currentSoundPressureLevel.roundTo(1)

    val currentLeqMetrics by viewModel.laeqMetricsFlow.collectAsStateWithLifecycle()
    val playPauseButtonViewModel by viewModel.playPauseButtonViewModelFlow.collectAsStateWithLifecycle()


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

                    Text(
                        text = if (roundedSpl.isInVuMeterRange()) roundedSpl.toString() else "-",
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
                        onClick = viewModel::toggleAudioSource,
                        viewModel = playPauseButtonViewModel,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            VuMeter(
                ticks = viewModel.vuMeterTicks,
                value = currentSoundPressureLevel,
            )
        }
    }
}
