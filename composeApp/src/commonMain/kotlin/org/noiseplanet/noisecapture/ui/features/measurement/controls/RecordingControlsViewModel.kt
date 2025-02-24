package org.noiseplanet.noisecapture.ui.features.measurement.controls

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_end_recording_button_title
import noisecapture.composeapp.generated.resources.measurement_start_recording_button_title
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.services.audio.LiveAudioService
import org.noiseplanet.noisecapture.services.measurement.MeasurementRecordingService
import org.noiseplanet.noisecapture.ui.components.button.ButtonStyle
import org.noiseplanet.noisecapture.ui.components.button.ButtonViewModel

class RecordingControlsViewModel : KoinComponent {

    // - Properties

    private val measurementRecordingService: MeasurementRecordingService by inject()
    private val liveAudioService: LiveAudioService by inject()

    val showPlayPauseButton: Flow<Boolean> = measurementRecordingService.isRecordingFlow

    val playPauseButtonViewModel = ButtonViewModel(
        onClick = {
            if (liveAudioService.isRunning) {
                liveAudioService.stopListening()
            } else {
                liveAudioService.startListening()
            }
        },
        icon = liveAudioService.isRunningFlow.map { isRunning ->
            if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow
        },
        style = liveAudioService.isRunningFlow.map { isRunning ->
            if (isRunning) ButtonStyle.OUTLINED else ButtonStyle.PRIMARY
        },
    )

    val startStopRecordingButtonViewModel: ButtonViewModel = ButtonViewModel(
        onClick = {
            if (measurementRecordingService.isRecording) {
                measurementRecordingService.endAndSave()
            } else {
                measurementRecordingService.start()
            }
        },
        title = measurementRecordingService.isRecordingFlow.map { isRecording ->
            if (isRecording) {
                Res.string.measurement_end_recording_button_title
            } else {
                Res.string.measurement_start_recording_button_title
            }
        },
        icon = measurementRecordingService.isRecordingFlow.map { isRecording ->
            if (isRecording) null else Icons.Filled.Mic
        },
        style = measurementRecordingService.isRecordingFlow.map { isRecording ->
            if (isRecording) ButtonStyle.SECONDARY else ButtonStyle.PRIMARY
        }
    )
}
