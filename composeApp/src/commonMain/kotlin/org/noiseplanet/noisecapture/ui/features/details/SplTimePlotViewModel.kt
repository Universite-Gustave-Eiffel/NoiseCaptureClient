package org.noiseplanet.noisecapture.ui.features.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapNotNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.util.stateInWhileSubscribed


class SplTimePlotViewModel(
    measurementId: String,
) : ViewModel(), KoinComponent {

    // - Properties

    private val measurementService: MeasurementService by inject()

    val plotDataFlow: StateFlow<Map<Long, Double>> = measurementService
        .getMeasurementFlow(measurementId)
        .mapNotNull { measurement ->
            measurement?.summary?.leqOverTime
        }
        .stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = emptyMap()
        )
}
