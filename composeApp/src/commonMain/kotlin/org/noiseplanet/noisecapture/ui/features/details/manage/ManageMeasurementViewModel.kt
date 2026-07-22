package org.noiseplanet.noisecapture.ui.features.details.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.details_delete_measurement_audio_dialog_text
import noisecapture.composeapp.generated.resources.details_delete_measurement_dialog_text
import noisecapture.composeapp.generated.resources.details_menu_delete_audio_description
import noisecapture.composeapp.generated.resources.details_menu_delete_audio_title
import noisecapture.composeapp.generated.resources.details_menu_delete_whole_description
import noisecapture.composeapp.generated.resources.details_menu_delete_whole_title
import noisecapture.composeapp.generated.resources.details_menu_export_audio_description
import noisecapture.composeapp.generated.resources.details_menu_export_audio_title
import noisecapture.composeapp.generated.resources.details_menu_export_geojson_description
import noisecapture.composeapp.generated.resources.details_menu_export_geojson_title
import noisecapture.composeapp.generated.resources.details_menu_export_raw_description
import noisecapture.composeapp.generated.resources.details_menu_export_raw_title
import org.jetbrains.compose.resources.StringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.services.storage.FileSystemService
import org.noiseplanet.noisecapture.util.stateInWhileSubscribed


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
        ) : ViewState
    }

    data class DeleteConfirmDialogState(
        val text: StringResource,
        val onDismissRequest: () -> Unit,
        val onConfirm: () -> Unit,
    )

    data class MenuItem(
        val label: StringResource,
        val supportingText: StringResource?,
        val onClick: () -> Unit,
    )


    // - Properties

    private val measurementService: MeasurementService by inject()
    private val fileSystemService: FileSystemService by inject()

    private val measurementFlow = measurementService.getMeasurementFlow(measurementId)
    private val measurement: Measurement?
        get() = (viewStateFlow.value as? ViewState.ContentReady)?.measurement

    private val _deleteConfirmDialogStateFlow = MutableStateFlow<DeleteConfirmDialogState?>(null)
    val deleteConfirmDialogState: StateFlow<DeleteConfirmDialogState?> =
        _deleteConfirmDialogStateFlow

    val deleteMenuItems: List<MenuItem>
        get() = measurement?.let { measurement ->
            val deleteWhole = MenuItem(
                label = Res.string.details_menu_delete_whole_title,
                supportingText = Res.string.details_menu_delete_whole_description,
                onClick = {
                    val state = DeleteConfirmDialogState(
                        text = Res.string.details_delete_measurement_audio_dialog_text,
                        onDismissRequest = { _deleteConfirmDialogStateFlow.tryEmit(null) },
                        onConfirm = { deleteMeasurementAudio() }
                    )
                    _deleteConfirmDialogStateFlow.tryEmit(state)
                },
            )
            if (measurement.recordedAudioUrl != null) {
                listOf(
                    MenuItem(
                        label = Res.string.details_menu_delete_audio_title,
                        supportingText = Res.string.details_menu_delete_audio_description,
                        onClick = {
                            val state = DeleteConfirmDialogState(
                                text = Res.string.details_delete_measurement_dialog_text,
                                onDismissRequest = { _deleteConfirmDialogStateFlow.tryEmit(null) },
                                onConfirm = { deleteMeasurementAudio() },
                            )
                            _deleteConfirmDialogStateFlow.tryEmit(state)
                        },
                    ),
                    deleteWhole,
                )
            } else {
                listOf(deleteWhole)
            }
        } ?: emptyList()

    val exportMenuItems: List<MenuItem>
        get() = measurement?.let { measurement ->
            val alwaysVisibleItems = listOf(
                MenuItem(
                    label = Res.string.details_menu_export_raw_title,
                    supportingText = Res.string.details_menu_export_raw_description,
                    onClick = { downloadRawData() },
                ),
                MenuItem(
                    label = Res.string.details_menu_export_geojson_title,
                    supportingText = Res.string.details_menu_export_geojson_description,
                    onClick = { exportToGeoJson() },
                ),
            )
            if (measurement.recordedAudioUrl != null) {
                listOf(
                    MenuItem(
                        label = Res.string.details_menu_export_audio_title,
                        supportingText = Res.string.details_menu_export_audio_description,
                        onClick = { downloadAudio() },
                    )
                ) + alwaysVisibleItems
            } else {
                alwaysVisibleItems
            }
        } ?: emptyList()

    val viewStateFlow: StateFlow<ViewState> = measurementFlow
        .filterNotNull()
        .map { measurement ->
            ViewState.ContentReady(
                measurement,
                measurementSize = measurementService.getMeasurementSize(measurement.uuid),
                audioFileSize = measurement.recordedAudioUrl?.let {
                    fileSystemService.size(it)
                },
            )
        }
        .stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = ViewState.Loading
        )


    // - Public functions

    fun deleteMeasurementAudio() {
        measurement?.let {
            viewModelScope.launch {
                measurementService.deleteMeasurementAssociatedAudio(it)
            }
        }
    }

    fun deleteMeasurement() {
        measurement?.let {
            viewModelScope.launch {
                measurementService.deleteMeasurement(it)
            }
        }
    }

    fun downloadRawData() {
        measurement?.let {
            viewModelScope.launch {
                measurementService.downloadRawMeasurement(it.uuid)
            }
        }
    }

    fun downloadAudio() {
        measurement?.recordedAudioUrl?.let {
            viewModelScope.launch {
                fileSystemService.download(it)
            }
        }
    }

    fun exportToGeoJson() {
        viewModelScope.launch(Dispatchers.Default) {
            // Download as geojson file
            fileSystemService.download("measurement/geojson/$measurementId.geojson")
        }
    }
}
