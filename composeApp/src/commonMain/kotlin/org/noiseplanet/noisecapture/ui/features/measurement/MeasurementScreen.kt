package org.noiseplanet.noisecapture.ui.features.measurement

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterView
import org.noiseplanet.noisecapture.ui.features.measurement.controls.RecordingControls

const val DEFAULT_SAMPLE_RATE = 48000.0

val NOISE_LEVEL_FONT_SIZE = TextUnit(50F, TextUnitType.Sp)
val SPECTRUM_PLOT_SQUARE_WIDTH = 10.dp
val SPECTRUM_PLOT_SQUARE_OFFSET = 1.dp

@Composable
fun MeasurementScreen(
    viewModel: MeasurementScreenViewModel,
) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    viewModel.setupAudioSource()
                }

                Lifecycle.Event.ON_PAUSE -> {
                    // If there is no ongoing measurement recording, pause audio source
                    if (!viewModel.isRecording) {
                        viewModel.stopAudioSource()
                    }
                }

                Lifecycle.Event.ON_RESUME -> {
                    // If there is no ongoing measurement recording, resume audio source
                    if (!viewModel.isRecording) {
                        viewModel.startAudioSource()
                    }
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            if (viewModel.isRecording) {
                viewModel.endRecording()
            }
            viewModel.releaseAudioSource()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints {
            if (maxWidth > maxHeight) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxWidth(.5F)) {
                        SoundLevelMeterView(viewModel = viewModel.soundLevelMeterViewModel)
                        RecordingControls(viewModel = viewModel.recordingControlsViewModel)
                    }
                    Column(modifier = Modifier) {
                        MeasurementPager()
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    SoundLevelMeterView(viewModel = viewModel.soundLevelMeterViewModel)
                    RecordingControls(
                        viewModel = viewModel.recordingControlsViewModel,
                        modifier = Modifier.height(IntrinsicSize.Min)
                    )
                    MeasurementPager()
                }
            }
        }
    }
}
