package org.noiseplanet.noisecapture.ui.features.details

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.model.dao.Measurement

val measurementDetailsModule = module {

    viewModel { (measurement: Measurement) ->
        MeasurementDetailsChartsViewModel(measurement)
    }
}
