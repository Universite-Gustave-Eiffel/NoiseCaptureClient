package org.noiseplanet.noisecapture.ui.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.util.stateInWhileSubscribed

class HistoryItemViewModel(measurementId: String) : ViewModel(), KoinComponent {

    // - Properties

    private val measurementService: MeasurementService by inject()

    val measurementFlow: StateFlow<Measurement?> = measurementService
        .getMeasurementFlow(measurementId)
        .stateInWhileSubscribed(viewModelScope, initialValue = null)
}
