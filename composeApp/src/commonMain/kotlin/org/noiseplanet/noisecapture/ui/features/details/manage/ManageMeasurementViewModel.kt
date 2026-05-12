package org.noiseplanet.noisecapture.ui.features.details.manage

import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.delete
import noisecapture.composeapp.generated.resources.details_delete_button
import noisecapture.composeapp.generated.resources.details_delete_measurement_audio_dialog_text
import noisecapture.composeapp.generated.resources.details_delete_measurement_dialog_text
import noisecapture.composeapp.generated.resources.details_delete_measurement_dialog_title
import noisecapture.composeapp.generated.resources.details_export_button
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
import noisecapture.composeapp.generated.resources.download
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.services.storage.FileSystemService
import org.noiseplanet.noisecapture.ui.components.button.NCButtonColors
import org.noiseplanet.noisecapture.ui.components.button.NCButtonViewModel
import org.noiseplanet.noisecapture.util.geo.GeoJsonBuilder
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


    // - Properties

    private val measurementService: MeasurementService by inject()
    private val fileSystemService: FileSystemService by inject()

    private val measurementFlow = measurementService.getMeasurementFlow(measurementId)
    private val measurement: Measurement?
        get() = (viewStateFlow.value as? ViewState.ContentReady)?.measurement

    private val _deleteConfirmationDialogViewModelFlow =
        MutableStateFlow<DeleteConfirmationDialogViewModel?>(null)
    val deleteConfirmationDialogViewModelFlow: StateFlow<DeleteConfirmationDialogViewModel?> =
        _deleteConfirmationDialogViewModelFlow

    val deleteButtonViewModel = NCButtonViewModel(
        title = Res.string.details_delete_button,
        icon = Res.drawable.delete,
        colors = {
            NCButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    )

    private val deleteAudioConfirmationViewModel = DeleteConfirmationDialogViewModel(
        title = Res.string.details_delete_measurement_dialog_title,
        text = Res.string.details_delete_measurement_audio_dialog_text,
        onDismissRequest = { _deleteConfirmationDialogViewModelFlow.tryEmit(null) },
        onConfirm = {
            deleteMeasurementAudio()
            _deleteConfirmationDialogViewModelFlow.tryEmit(null)
        }
    )

    private val deleteMeasurementConfirmationViewModel = DeleteConfirmationDialogViewModel(
        title = Res.string.details_delete_measurement_dialog_title,
        text = Res.string.details_delete_measurement_dialog_text,
        onDismissRequest = { _deleteConfirmationDialogViewModelFlow.tryEmit(null) },
        onConfirm = {
            deleteMeasurement()
            _deleteConfirmationDialogViewModelFlow.tryEmit(null)
        }
    )

    val deleteMenuItems: List<ManageMeasurementMenuItem>
        get() = measurement?.let { measurement ->
            val deleteWhole = ManageMeasurementMenuItem(
                label = Res.string.details_menu_delete_whole_title,
                supportingText = Res.string.details_menu_delete_whole_description,
                onClick = {
                    _deleteConfirmationDialogViewModelFlow.tryEmit(
                        deleteMeasurementConfirmationViewModel
                    )
                },
            )
            if (measurement.recordedAudioUrl != null) {
                listOf(
                    ManageMeasurementMenuItem(
                        label = Res.string.details_menu_delete_audio_title,
                        supportingText = Res.string.details_menu_delete_audio_description,
                        onClick = {
                            _deleteConfirmationDialogViewModelFlow.tryEmit(
                                deleteAudioConfirmationViewModel
                            )
                        },
                    ),
                    deleteWhole,
                )
            } else {
                listOf(deleteWhole)
            }
        } ?: emptyList()

    val exportButtonViewModel = NCButtonViewModel(
        title = Res.string.details_export_button,
        icon = Res.drawable.download,
        colors = {
            NCButtonColors.Defaults.secondary()
        }
    )

    val exportMenuItems: List<ManageMeasurementMenuItem>
        get() = measurement?.let { measurement ->
            val alwaysVisibleItems = listOf(
                ManageMeasurementMenuItem(
                    label = Res.string.details_menu_export_raw_title,
                    supportingText = Res.string.details_menu_export_raw_description,
                    onClick = { downloadRawData() },
                ),
                ManageMeasurementMenuItem(
                    label = Res.string.details_menu_export_geojson_title,
                    supportingText = Res.string.details_menu_export_geojson_description,
                    onClick = { exportToGeoJson() },
                ),
            )
            if (measurement.recordedAudioUrl != null) {
                listOf(
                    ManageMeasurementMenuItem(
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
            // Construct feature collection from measurement location and leq sequences
            val leqs = measurementService.getLeqSequenceForMeasurement(measurementId)
            val locations = measurementService.getLocationSequenceForMeasurement(measurementId)
            val geoJson = GeoJsonBuilder.fromMeasurement(leqs, locations)
            // Download as geojson file
            fileSystemService.downloadGeoJson(geoJson, "$measurementId.geojson")
        }
    }
}
