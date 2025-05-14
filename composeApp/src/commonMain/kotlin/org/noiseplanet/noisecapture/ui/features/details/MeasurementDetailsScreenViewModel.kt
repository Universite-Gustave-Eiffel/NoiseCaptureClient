package org.noiseplanet.noisecapture.ui.features.details

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
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

    val measurementFlow: Flow<Measurement?> = measurementService.getMeasurementFlow(measurementId)


    // - ScreenViewModel

    override val title: StringResource
        get() = Res.string.measurement_details_title
}
