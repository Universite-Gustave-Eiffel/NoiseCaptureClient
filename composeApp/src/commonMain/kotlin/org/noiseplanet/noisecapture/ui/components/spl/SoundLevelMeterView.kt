package org.noiseplanet.noisecapture.ui.components.spl

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.ui.theme.NotoSansMono
import org.noiseplanet.noisecapture.util.isInVuMeterRange


@Composable
fun SoundLevelMeterView() {

    // - Properties

    val viewModel: SoundLevelMeterViewModel = koinViewModel()

    val currentSpl by viewModel.soundPressureLevelFlow
        .collectAsStateWithLifecycle()
    val currentLeqMetrics by viewModel.laeqMetricsFlow
        .collectAsStateWithLifecycle()
    val playPauseButtonViewModel by viewModel.playPauseButtonViewModelFlow
        .collectAsStateWithLifecycle()

    val currentSplColor by animateColorAsState(NoiseLevelColorRamp.getColorForSPLValue(currentSpl))


    // - Layout

    Box(
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
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
                        text = if (currentSpl.isInVuMeterRange()) currentSpl.toString() else "-",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.NotoSansMono,
                            fontSize = 36.sp,
                            color = currentSplColor
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
                valueFlow = viewModel.soundPressureLevelFlow,
            )
        }
    }
}
