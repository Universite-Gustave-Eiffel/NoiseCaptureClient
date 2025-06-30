package org.noiseplanet.noisecapture.ui.features.details

import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_details_delete_measurement_audio_button
import noisecapture.composeapp.generated.resources.measurement_details_delete_measurement_button
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.services.audio.AudioRecordingService
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.ui.components.button.NCButtonColors
import org.noiseplanet.noisecapture.ui.components.button.NCButtonStyle
import org.noiseplanet.noisecapture.ui.components.button.NCButtonViewModel


class ManageMeasurementViewModel(
    val measurementId: String,
) : ViewModel(), KoinComponent {

    // - ViewState

    sealed interface ViewState {

        data object Loading : ViewState

        data class ContentReady(
            val measurement: Measurement,
            val measurementSize: Long?,
            val audioFileSize: Long?,
            val deleteMeasurementButtonViewModel: NCButtonViewModel,
            val deleteAudioButtonViewModel: NCButtonViewModel?,
        ) : ViewState
    }


    // - Properties

    private val measurementService: MeasurementService by inject()
    private val audioRecordingService: AudioRecordingService by inject()

    private val measurementFlow = measurementService.getMeasurementFlow(measurementId)
    private val measurement: Measurement?
        get() = (viewStateFlow.value as? ViewState.ContentReady)?.measurement

    private val deleteMeasurementButtonViewModel = NCButtonViewModel(
        title = Res.string.measurement_details_delete_measurement_button,
        colors = {
            NCButtonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            )
        }
    )

    private val deleteAudioButtonViewModel = NCButtonViewModel(
        title = Res.string.measurement_details_delete_measurement_audio_button,
        style = NCButtonStyle.TEXT,
        colors = {
            NCButtonColors.Defaults.text().copy(
                contentColor = MaterialTheme.colorScheme.error
            )
        }
    )

    val viewStateFlow: StateFlow<ViewState> = measurementFlow
        .filterNotNull()
        .map { measurement ->
            ViewState.ContentReady(
                measurement,
                measurementSize = measurementService.getMeasurementSize(measurement.uuid),
                audioFileSize = measurement.recordedAudioUrl?.let { audioUrl ->
                    audioRecordingService.getFileSize(audioUrl)
                },
                deleteMeasurementButtonViewModel = deleteMeasurementButtonViewModel,
                deleteAudioButtonViewModel = measurement.recordedAudioUrl?.let {
                    deleteAudioButtonViewModel
                },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ViewState.Loading
        )


    // - Public functions

    fun deleteMeasurementAudio() {
        measurement?.let {
            viewModelScope.launch {
                measurementService.deleteMeasurementAssociatedAudio(it.uuid)
            }
        }
    }

    fun deleteMeasurement() {
        measurement?.let {
            viewModelScope.launch {
                measurementService.deleteMeasurement(it.uuid)
            }
        }
    }
}
