package org.noiseplanet.noisecapture.ui.features.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_details_title
import org.jetbrains.compose.resources.StringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel


class MeasurementDetailsScreenViewModel(
    private val measurementId: String,
) : ViewModel(), ScreenViewModel, KoinComponent {

    // - Properties

    private val measurementService: MeasurementService by inject()
    private val measurementFlow = measurementService.getMeasurementFlow(measurementId)
        .map { measurement ->
            // If measurement has no summary yet, we need to calculate it.
            // The view should display a loading state until measurement is ready.
            if (measurement != null && measurement.summary == null) {
                measurementService.calculateSummary(measurement)
            } else {
                measurement
            }
        }

    val viewState: StateFlow<MeasurementDetailsScreenViewState> = measurementFlow
        .map { measurement ->
            measurement?.let {
                MeasurementDetailsScreenViewState.ContentReady(it)
            } ?: MeasurementDetailsScreenViewState.Error
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = MeasurementDetailsScreenViewState.Loading
        )


    // - ScreenViewModel

    override val title: StringResource
        get() = Res.string.measurement_details_title


    // - Public functions

    fun deleteMeasurement() {
        viewModelScope.launch {
            measurementService.deleteMeasurement(measurementId)
        }
    }
}


sealed class MeasurementDetailsScreenViewState {

    data class ContentReady(
        val measurement: Measurement,
    ) : MeasurementDetailsScreenViewState()

    data object Loading : MeasurementDetailsScreenViewState()
    data object Error : MeasurementDetailsScreenViewState()
}
