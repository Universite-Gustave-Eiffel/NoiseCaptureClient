package org.noiseplanet.noisecapture.ui.features.history

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel


class HistoryScreenViewModel : ViewModel(), ScreenViewModel, KoinComponent {

    // - Properties

    private val measurementService: MeasurementService by inject()

    val measurementsFlow: Flow<List<Measurement>> = measurementService.getAllMeasurementsFlow()
}
