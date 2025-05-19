package org.noiseplanet.noisecapture.ui.features.measurement

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.koin.compose.module.rememberKoinModules
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterView
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterViewModel
import org.noiseplanet.noisecapture.ui.features.measurement.controls.RecordingControls
import org.noiseplanet.noisecapture.ui.features.measurement.controls.RecordingControlsViewModel


@OptIn(KoinExperimentalAPI::class)
@Composable
fun MeasurementScreen(
    viewModel: MeasurementScreenViewModel,
) {
    // - DI

    rememberKoinModules(unloadOnForgotten = true) {
        listOf(measurementModule)
    }


    // - Properties

    val soundLevelMeterViewModel: SoundLevelMeterViewModel = koinViewModel()
    val recordingControlsViewModel: RecordingControlsViewModel = koinViewModel()


    // - Layout

    Surface(
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
        color = Color.White
    ) {
        BoxWithConstraints {
            if (maxWidth > maxHeight) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxWidth(.5F)) {
                        SoundLevelMeterView(viewModel = soundLevelMeterViewModel)
                        RecordingControls(viewModel = recordingControlsViewModel)
                    }
                    Column(modifier = Modifier) {
                        MeasurementPager()
                    }
                }
            } else {
                Column {
                    SoundLevelMeterView(viewModel = soundLevelMeterViewModel)
                    MeasurementPager(modifier = Modifier.fillMaxWidth().weight(1f))
                    RecordingControls(
                        viewModel = recordingControlsViewModel,
                    )
                }
            }
        }
    }
}
