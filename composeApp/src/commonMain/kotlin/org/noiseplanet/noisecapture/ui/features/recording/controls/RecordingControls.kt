package org.noiseplanet.noisecapture.ui.features.recording.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.noiseplanet.noisecapture.ui.components.button.NCButton

/**
 * Start/Stop and Play/Pause buttons to manage current recording
 *
 * @param onMeasurementDone Called when measurement recording ends, with UUID as parameter.
 */
@Composable
fun RecordingControls(
    onMeasurementDone: (String) -> Unit,
) {
    // - Properties

    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val viewModel: RecordingControlsViewModel = koinViewModel()

    val showPlayPauseButton by viewModel.showPlayPauseButton.collectAsStateWithLifecycle(false)
    val playPauseButtonViewModel by viewModel.playPauseButtonViewModelFlow.collectAsStateWithLifecycle()
    val startStopButtonViewModel by viewModel.startStopButtonViewModelFlow.collectAsStateWithLifecycle()


    // - Lifecycle

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.registerListener(onMeasurementDone)
                }

                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.deregisterListener()
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    // - Layout

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
    ) {
        AnimatedVisibility(
            visible = showPlayPauseButton,
            enter = fadeIn() + slideInHorizontally() + expandHorizontally(),
            exit = fadeOut() + slideOutHorizontally() + shrinkHorizontally(),
        ) {
            NCButton(
                onClick = viewModel::toggleAudioSource,
                viewModel = playPauseButtonViewModel,
                modifier = Modifier.padding(end = 16.dp).size(50.dp)
            )
        }
        NCButton(
            onClick = viewModel::toggleRecording,
            viewModel = startStopButtonViewModel,
            modifier = Modifier.height(50.dp)
        )
    }
}
