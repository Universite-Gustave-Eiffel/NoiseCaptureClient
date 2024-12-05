package org.noiseplanet.noisecapture.ui.features.measurement.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RecordingControls(
    viewModel: RecordingControlsViewModel,
    modifier: Modifier = Modifier,
) {
    val playPauseButtonTitle by viewModel.playPauseButtonTitle.collectAsState("")
    val startStopButtonTitle by viewModel.startStopButtonTitle.collectAsState("")
    val isRecording by viewModel.isRecording.collectAsState(false)

    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (isRecording) {
                Button(onClick = viewModel.onPlayPauseButtonClick) {
                    Text(playPauseButtonTitle)
                }
            }
            Button(onClick = viewModel.onStartStopButtonClick) {
                Text(startStopButtonTitle)
            }
        }
    }
}
