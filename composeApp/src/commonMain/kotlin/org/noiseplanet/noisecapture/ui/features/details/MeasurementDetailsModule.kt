package org.noiseplanet.noisecapture.ui.features.details

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val measurementDetailsModule = module {

    viewModel { (measurementId: String) ->
        MeasurementDetailsScreenViewModel(measurementId)
    }
}
