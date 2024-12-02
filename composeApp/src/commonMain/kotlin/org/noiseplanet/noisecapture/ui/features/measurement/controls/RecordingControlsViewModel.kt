package org.noiseplanet.noisecapture.ui.features.measurement.controls

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecordingControlsViewModel(
    val isPlaying: Flow<Boolean>,
    val isRecording: Flow<Boolean>,
    val onPlayPauseButtonClick: () -> Unit,
    val onStartStopButtonClick: () -> Unit,
) {

    // TODO: Localise this
    val playPauseButtonTitle: Flow<String> = isPlaying.map { isPlaying ->
        if (isPlaying) {
            "Pause"
        } else {
            "Resume"
        }
    }

    // TODO: Localise this
    val startStopButtonTitle: Flow<String> = isRecording.map { isRecording ->
        if (isRecording) {
            "End measurement"
        } else {
            "Start a new measurement"
        }
    }
}
