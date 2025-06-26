package org.noiseplanet.noisecapture.ui.features.details

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.ui.components.audioplayer.AudioPlayerViewModel

val measurementDetailsModule = module {

    viewModel { (measurement: Measurement) ->
        MeasurementDetailsChartsViewModel(measurement)
    }

    viewModel { (filePath: String) ->
        AudioPlayerViewModel(filePath)
    }

    viewModel { (measurement: Measurement) ->
        ManageMeasurementViewModel(measurement)
    }
}
