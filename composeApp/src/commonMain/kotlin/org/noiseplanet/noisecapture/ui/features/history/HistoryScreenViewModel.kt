package org.noiseplanet.noisecapture.ui.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.history_title
import org.jetbrains.compose.resources.StringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel
import org.noiseplanet.noisecapture.util.stateInWhileSubscribed


class HistoryScreenViewModel : ViewModel(), ScreenViewModel, KoinComponent {

    // - Properties

    private val measurementService: MeasurementService by inject()

    val measurementsFlow: StateFlow<List<Measurement>> = measurementService.getAllMeasurementsFlow()
        .stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = emptyList(),
        )


    // - ScreenViewModel

    override val title: StringResource
        get() = Res.string.history_title
}
