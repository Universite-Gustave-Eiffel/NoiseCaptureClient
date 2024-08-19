package org.noiseplanet.noisecapture.ui.features.measurement

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.noiseplanet.noisecapture.ui.features.measurement.indicators.AcousticIndicatorsView

const val DEFAULT_SAMPLE_RATE = 48000.0

val NOISE_LEVEL_FONT_SIZE = TextUnit(50F, TextUnitType.Sp)
val SPECTRUM_PLOT_SQUARE_WIDTH = 10.dp
val SPECTRUM_PLOT_SQUARE_OFFSET = 1.dp

@OptIn(KoinExperimentalAPI::class)
@Composable
fun MeasurementScreen(
    viewModel: MeasurementScreenViewModel = koinInject(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
) {

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.startRecordingAudio()
                Lifecycle.Event.ON_STOP -> viewModel.stopRecordingAudio()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
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
                        AcousticIndicatorsView(viewModel = koinViewModel())
                    }
                    Column(modifier = Modifier) {
                        MeasurementPager()
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    AcousticIndicatorsView(viewModel = koinViewModel())
                    MeasurementPager()
                }
            }
        }
    }
}
