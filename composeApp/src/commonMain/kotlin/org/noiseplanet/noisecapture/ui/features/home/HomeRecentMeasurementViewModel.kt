package org.noiseplanet.noisecapture.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.services.measurement.MeasurementService


class HomeRecentMeasurementViewModel(
    val measurementId: String,
) : ViewModel(), KoinComponent {

    // - Properties

    private val measurementService: MeasurementService by inject()
    private var measurementMutableFlow = MutableStateFlow<Measurement?>(null)

    val measurementFlow: StateFlow<Measurement?> = measurementMutableFlow


    // - Lifecycle

    init {
        viewModelScope.launch(Dispatchers.Default) {
            measurementMutableFlow.tryEmit(
                measurementService.getMeasurement(measurementId)
            )
        }
    }
}
