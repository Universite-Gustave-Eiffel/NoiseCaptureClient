package org.noiseplanet.noisecapture.ui.features.recording.controls

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_end_recording_button_title
import noisecapture.composeapp.generated.resources.measurement_start_recording_button_title
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.services.audio.LiveAudioService
import org.noiseplanet.noisecapture.services.measurement.MeasurementRecordingService
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.ui.components.button.IconNCButtonViewModel
import org.noiseplanet.noisecapture.ui.components.button.NCButtonColors
import org.noiseplanet.noisecapture.ui.components.button.NCButtonStyle
import org.noiseplanet.noisecapture.ui.components.button.NCButtonViewModel

class RecordingControlsViewModel : ViewModel(), KoinComponent {

    // - Properties

    private val measurementService: MeasurementService by inject()
    private val measurementRecordingService: MeasurementRecordingService by inject()
    private val liveAudioService: LiveAudioService by inject()

    val showPlayPauseButton: Flow<Boolean> = measurementRecordingService.isRecordingFlow

    val playPauseButtonViewModelFlow: StateFlow<NCButtonViewModel> = liveAudioService.isRunningFlow
        .map { isRunning ->
            getPlayPauseButtonViewModel(isRunning)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            getPlayPauseButtonViewModel(liveAudioService.isRunning)
        )

    val startStopButtonViewModelFlow: StateFlow<NCButtonViewModel> =
        measurementRecordingService.isRecordingFlow.map { isRecording ->
            getStartStopButtonViewModel(isRecording)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            getStartStopButtonViewModel(isRecording)
        )

    val isRecording: Boolean
        get() = measurementRecordingService.isRecording

    val ongoingMeasurementUuid: String?
        get() = measurementService.ongoingMeasurementUuid


    // - Public functions

    fun toggleAudioSource() {
        if (liveAudioService.isRunning) {
            liveAudioService.stopListening()
        } else {
            liveAudioService.startListening()
        }
    }

    fun toggleRecording() {
        if (measurementRecordingService.isRecording) {
            measurementRecordingService.endAndSave()
        } else {
            measurementRecordingService.start()
        }
    }

    fun registerListener(onMeasurementDone: (String) -> Unit) {
        measurementRecordingService.onMeasurementDone =
            object : MeasurementRecordingService.OnMeasurementDoneListener {
                override fun onDone(measurementUuid: String) {
                    onMeasurementDone(measurementUuid)
                }
            }
    }

    fun deregisterListener() {
        measurementRecordingService.onMeasurementDone = null
    }


    // - Private functions

    private fun getPlayPauseButtonViewModel(isAudioSourceRunning: Boolean): NCButtonViewModel {
        val icon = if (isAudioSourceRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow
        val style = if (isAudioSourceRunning) NCButtonStyle.OUTLINED else NCButtonStyle.FILLED
        val colors = @Composable {
            if (isAudioSourceRunning) {
                NCButtonColors.Defaults.outlined()
            } else {
                NCButtonColors.Defaults.primary()
            }
        }

        return IconNCButtonViewModel(
            icon = icon,
            style = style,
            colors = colors,
        )
    }

    private fun getStartStopButtonViewModel(isRecording: Boolean): NCButtonViewModel {
        val title = if (isRecording) {
            Res.string.measurement_end_recording_button_title
        } else {
            Res.string.measurement_start_recording_button_title
        }
        val icon = if (isRecording) null else Icons.Filled.Mic

        return NCButtonViewModel(
            title = title,
            icon = icon,
        )
    }
}
