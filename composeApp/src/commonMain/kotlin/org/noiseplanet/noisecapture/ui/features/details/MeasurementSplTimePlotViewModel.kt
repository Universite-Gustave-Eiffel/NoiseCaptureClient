package org.noiseplanet.noisecapture.ui.features.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.LeqSequenceFragment
import org.noiseplanet.noisecapture.services.measurement.MeasurementService


class MeasurementSplTimePlotViewModel(
    measurementId: String,
) : ViewModel(), KoinComponent {

    // - Properties

    private val measurementService: MeasurementService by inject()
    private val _leqSequenceFlow = MutableStateFlow<List<LeqSequenceFragment>>(emptyList())

    val leqSequenceFlow: StateFlow<List<LeqSequenceFragment>> = _leqSequenceFlow


    // - Lifecycle

    init {
        viewModelScope.launch(Dispatchers.Default) {
            _leqSequenceFlow.emit(
                measurementService.getLeqSequenceForMeasurement(measurementId)
            )
        }
    }
}
