package org.noiseplanet.noisecapture.ui.components.spl

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.pause
import noisecapture.composeapp.generated.resources.play_arrow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.NCButton
import org.noiseplanet.noisecapture.ui.components.secondaryContainerColors
import org.noiseplanet.noisecapture.ui.theme.Noise
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.util.isInVuMeterRange


@Composable
fun SoundLevelMeterView(
    showPermissionPrompt: (Permission) -> Unit = {},
) {

    // - Properties

    val viewModel: SoundLevelMeterViewModel = koinViewModel()

    val currentSpl by viewModel.soundPressureLevelFlow
        .collectAsStateWithLifecycle()
    val currentLeqMetrics by viewModel.laeqMetricsFlow
        .collectAsStateWithLifecycle()
    val isRunning by viewModel.isRunning
        .collectAsStateWithLifecycle()

    val currentSplColor by animateColorAsState(
        NoiseLevelColorRamp.getColorForSPLValue(
            value = currentSpl,
            palette = NoiseLevelColorRamp.paletteDarker,
        )
    )


    // - Layout

    Box(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(start = 16.dp, end = 10.dp).fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = stringResource(viewModel.currentDbALabel),
                        style = MaterialTheme.typography.labelLarge,
                    )

                    Text(
                        text = if (currentSpl.isInVuMeterRange()) currentSpl.toString() else "-",
                        style = MaterialTheme.typography.headlineLarge,
                        color = currentSplColor,
                    )
                }

                if (viewModel.showMinMaxSPL) {
                    LAeqMetricsView(metrics = currentLeqMetrics)
                }

                if (viewModel.showPlayPauseButton) {
                    NCButton(
                        onClick = { viewModel.toggleAudioSource(showPermissionPrompt) },
                        content = ButtonContent(icon = if (isRunning) Res.drawable.pause else Res.drawable.play_arrow),
                        colors = Color.Noise.two.secondaryContainerColors(),
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
